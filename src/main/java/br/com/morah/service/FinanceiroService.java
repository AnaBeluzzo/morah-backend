package br.com.morah.service;

import br.com.morah.dto.CobrancaPage;
import br.com.morah.dto.DocumentoFinanceiroCreateRequest;
import br.com.morah.dto.DocumentoFinanceiroPage;
import br.com.morah.dto.FalhaGeracaoCobranca;
import br.com.morah.dto.GerarCobrancasResponse;
import br.com.morah.dto.InadimplenciaResumo;
import br.com.morah.dto.PageMetadata;
import br.com.morah.dto.PixResponse;
import br.com.morah.dto.StatusCobranca;
import br.com.morah.dto.TaxaCreateRequest;
import br.com.morah.exception.ApiException;
import br.com.morah.model.Cobranca;
import br.com.morah.model.DocumentoFinanceiro;
import br.com.morah.model.Taxa;
import br.com.morah.model.Unidade;
import br.com.morah.pattern.BaixaManualProcessador;
import br.com.morah.pattern.EstrategiaRateio;
import br.com.morah.pattern.NotificacaoFinanceiro;
import br.com.morah.pattern.WebhookPagamentoProcessador;
import br.com.morah.repository.CobrancaRepository;
import br.com.morah.repository.CondominioRepository;
import br.com.morah.repository.DocumentoFinanceiroRepository;
import br.com.morah.repository.PessoaPerfilRepository;
import br.com.morah.repository.TaxaRepository;
import br.com.morah.repository.UnidadeRepository;
import br.com.morah.security.UsuarioLogado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
public class FinanceiroService {

    private static final Duration VALIDADE_PIX = Duration.ofMinutes(30);
    // gateway simulado: o "copia e cola" é esta base + id + valor
    private static final String CODIGO_PIX_BASE = "00020126MORAH.PIX.SIMULADO";

    private final CobrancaRepository cobrancaRepository;
    private final TaxaRepository taxaRepository;
    private final DocumentoFinanceiroRepository documentoFinanceiroRepository;
    private final UnidadeRepository unidadeRepository;
    private final CondominioRepository condominioRepository;
    private final PessoaPerfilRepository pessoaPerfilRepository;
    private final List<EstrategiaRateio> estrategiasRateio;
    private final NotificacaoFinanceiro notificacaoFinanceiro;
    private final BaixaManualProcessador baixaManualProcessador;
    private final WebhookPagamentoProcessador webhookPagamentoProcessador;

    public FinanceiroService(CobrancaRepository cobrancaRepository, TaxaRepository taxaRepository,
                             DocumentoFinanceiroRepository documentoFinanceiroRepository,
                             UnidadeRepository unidadeRepository, CondominioRepository condominioRepository,
                             PessoaPerfilRepository pessoaPerfilRepository, List<EstrategiaRateio> estrategiasRateio,
                             NotificacaoFinanceiro notificacaoFinanceiro, BaixaManualProcessador baixaManualProcessador,
                             WebhookPagamentoProcessador webhookPagamentoProcessador) {
        this.cobrancaRepository = cobrancaRepository;
        this.taxaRepository = taxaRepository;
        this.documentoFinanceiroRepository = documentoFinanceiroRepository;
        this.unidadeRepository = unidadeRepository;
        this.condominioRepository = condominioRepository;
        this.pessoaPerfilRepository = pessoaPerfilRepository;
        this.estrategiasRateio = estrategiasRateio;
        this.notificacaoFinanceiro = notificacaoFinanceiro;
        this.baixaManualProcessador = baixaManualProcessador;
        this.webhookPagamentoProcessador = webhookPagamentoProcessador;
    }

    @Transactional(readOnly = true)
    public CobrancaPage buscarCobrancas(UsuarioLogado usuario, StatusCobranca status, String competencia,
                                        Long unidadeId, int page, int size) {
        // morador/proprietário: sempre a própria unidade (o parâmetro unidadeId é ignorado)
        Long unidadeFiltro = usuario.isMoradorOuProprietario() ? usuario.unidadeId() : unidadeId;

        // "pendente" e "atrasado" são o mesmo status gravado; a data de vencimento separa os dois
        StatusCobranca statusGravado = status == StatusCobranca.ATRASADO ? StatusCobranca.PENDENTE : status;
        LocalDate hoje = LocalDate.now();
        LocalDate vencidoAntesDe = status == StatusCobranca.ATRASADO ? hoje : null;
        LocalDate venceAPartirDe = status == StatusCobranca.PENDENTE ? hoje : null;

        Page<Cobranca> pagina = cobrancaRepository.buscarComFiltros(usuario.condominioId(), unidadeFiltro, competencia,
                statusGravado, vencidoAntesDe, venceAPartirDe, PageRequest.of(page, size));
        return new CobrancaPage(pagina.getContent().stream().map(br.com.morah.dto.Cobranca::de).toList(),
                PageMetadata.de(pagina));
    }

    @Transactional(readOnly = true)
    public br.com.morah.dto.Cobranca buscarCobranca(UsuarioLogado usuario, Long cobrancaId) {
        return br.com.morah.dto.Cobranca.de(buscarCobrancaPermitida(usuario, cobrancaId));
    }

    @Transactional(readOnly = true)
    public PixResponse gerarPix(UsuarioLogado usuario, Long cobrancaId) {
        Cobranca cobranca = buscarCobrancaPermitida(usuario, cobrancaId);
        if (cobranca.getStatus() == StatusCobranca.PAGO || cobranca.getStatus() == StatusCobranca.CANCELADO) {
            throw ApiException.conflito("Cobrança já está paga ou cancelada");
        }
        String codigo = CODIGO_PIX_BASE + "-" + cobranca.getId() + "-" + cobranca.calcularValorTotal().toPlainString();
        return new PixResponse(codigo, Instant.now().plus(VALIDADE_PIX));
    }

    @Transactional
    public br.com.morah.dto.Cobranca baixarCobrancaManualmente(Long cobrancaId) {
        return br.com.morah.dto.Cobranca.de(baixaManualProcessador.processar(cobrancaId));
    }

    @Transactional
    public void receberWebhookPagamento(HttpEntity<String> requisicao) {
        try {
            webhookPagamentoProcessador.processar(requisicao);
        } catch (ApiException e) {
            // o contrato do webhook só prevê 400 e 401: cobrança inexistente ou já quitada é payload inválido
            if (e.getStatus() == HttpStatus.NOT_FOUND || e.getStatus() == HttpStatus.CONFLICT) {
                throw ApiException.requisicaoInvalida(e.getMessage());
            }
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public DocumentoFinanceiroPage buscarDocumentosFinanceiros(UsuarioLogado usuario, int page, int size) {
        Page<DocumentoFinanceiro> pagina = documentoFinanceiroRepository
                .findByCondominioIdOrderByPublicadoEmDescIdDesc(usuario.condominioId(), PageRequest.of(page, size));
        return new DocumentoFinanceiroPage(pagina.getContent().stream().map(br.com.morah.dto.DocumentoFinanceiro::de).toList(),
                PageMetadata.de(pagina));
    }

    @Transactional
    public br.com.morah.dto.DocumentoFinanceiro criarDocumentoFinanceiro(UsuarioLogado usuario,
                                                                         DocumentoFinanceiroCreateRequest request) {
        DocumentoFinanceiro documento = new DocumentoFinanceiro();
        documento.setCondominio(condominioRepository.getReferenceById(usuario.condominioId()));
        documento.setTipo(request.tipo());
        documento.setTitulo(request.titulo());
        documento.setArquivoUrl(request.arquivoUrl());
        documento.setCompetencia(request.competencia());
        documento.setPublicadoEm(Instant.now());
        return br.com.morah.dto.DocumentoFinanceiro.de(documentoFinanceiroRepository.save(documento));
    }

    @Transactional
    public br.com.morah.dto.Taxa criarTaxa(UsuarioLogado usuario, TaxaCreateRequest request) {
        try {
            YearMonth.parse(request.competencia());
        } catch (DateTimeParseException e) {
            throw ApiException.requisicaoInvalida("Competência inválida: " + request.competencia());
        }
        Taxa taxa = new Taxa();
        taxa.setCondominio(condominioRepository.getReferenceById(usuario.condominioId()));
        taxa.setDescricao(request.descricao());
        taxa.setCompetencia(request.competencia());
        taxa.setValor(request.valor());
        taxa.setTipoRateio(request.tipoRateio());
        taxa.setVencimentoDia(request.vencimentoDia());
        return br.com.morah.dto.Taxa.de(taxaRepository.save(taxa));
    }

    /** Uma cobrança pendente por unidade; o valor vem da estratégia de rateio da taxa (Strategy). */
    @Transactional
    public GerarCobrancasResponse gerarCobrancasEmLote(UsuarioLogado usuario, Long taxaId) {
        Taxa taxa = taxaRepository.findByIdAndCondominioId(taxaId, usuario.condominioId())
                .orElseThrow(() -> ApiException.naoEncontrado("Taxa não encontrada"));
        EstrategiaRateio estrategia = estrategiasRateio.stream()
                .filter(e -> e.getTipo() == taxa.getTipoRateio())
                .findFirst().orElseThrow();
        List<Unidade> unidades = unidadeRepository.findByBlocoCondominioIdOrderById(usuario.condominioId());
        LocalDate vencimento = YearMonth.parse(taxa.getCompetencia()).atDay(taxa.getVencimentoDia());

        int geradas = 0;
        List<FalhaGeracaoCobranca> falhas = new ArrayList<>();
        for (Unidade unidade : unidades) {
            if (cobrancaRepository.existsByTaxaIdAndUnidadeId(taxaId, unidade.getId())) {
                falhas.add(new FalhaGeracaoCobranca(unidade.getId(), "Unidade já cobrada por esta taxa"));
                continue;
            }
            BigDecimal valor;
            try {
                valor = estrategia.calcularValor(taxa.getValor(), unidade, unidades.size());
            } catch (IllegalArgumentException e) {
                falhas.add(new FalhaGeracaoCobranca(unidade.getId(), e.getMessage()));
                continue;
            }
            Cobranca cobranca = new Cobranca();
            cobranca.setUnidade(unidade);
            cobranca.setTaxa(taxa);
            cobranca.setCompetencia(taxa.getCompetencia());
            cobranca.setVencimento(vencimento);
            cobranca.setValorOriginal(valor);
            // multa e juros nascem zerados
            cobranca.setMulta(BigDecimal.ZERO);
            cobranca.setJuros(BigDecimal.ZERO);
            cobranca.setStatus(StatusCobranca.PENDENTE);
            cobrancaRepository.save(cobranca);
            notificacaoFinanceiro.notificar(cobranca, pessoaPerfilRepository.findMoradoresByUnidadeId(unidade.getId()));
            geradas++;
        }
        return new GerarCobrancasResponse(taxaId, geradas, falhas);
    }

    /** Inadimplente = ao menos uma cobrança atrasada; em aberto = pendentes + atrasadas. */
    @Transactional(readOnly = true)
    public InadimplenciaResumo buscarResumoInadimplencia(Long condominioId) {
        int totalUnidades = unidadeRepository.findByBlocoCondominioIdOrderById(condominioId).size();
        List<Cobranca> emAberto = cobrancaRepository.findByUnidadeBlocoCondominioIdAndStatus(condominioId, StatusCobranca.PENDENTE);

        int inadimplentes = (int) emAberto.stream()
                .filter(c -> c.calcularStatus() == StatusCobranca.ATRASADO)
                .map(c -> c.getUnidade().getId())
                .distinct()
                .count();
        BigDecimal valorEmAberto = emAberto.stream()
                .map(Cobranca::calcularValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal percentual = totalUnidades == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(inadimplentes * 100L).divide(BigDecimal.valueOf(totalUnidades), 2, RoundingMode.HALF_UP);
        return new InadimplenciaResumo(totalUnidades, inadimplentes, percentual, valorEmAberto);
    }

    /** 404 se for de outro condomínio; 403 se morador/proprietário de outra unidade. */
    private Cobranca buscarCobrancaPermitida(UsuarioLogado usuario, Long cobrancaId) {
        Cobranca cobranca = cobrancaRepository.findByIdAndUnidadeBlocoCondominioId(cobrancaId, usuario.condominioId())
                .orElseThrow(() -> ApiException.naoEncontrado("Cobrança não encontrada"));
        usuario.verificarUnidade(cobranca.getUnidade().getId());
        return cobranca;
    }
}
