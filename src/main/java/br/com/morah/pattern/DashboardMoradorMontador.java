package br.com.morah.pattern;

import br.com.morah.dto.Dashboard;
import br.com.morah.dto.DashboardMorador;
import br.com.morah.dto.ModuloCodigo;
import br.com.morah.dto.Perfil;
import br.com.morah.dto.StatusAutorizacaoVisita;
import br.com.morah.dto.StatusCobranca;
import br.com.morah.dto.StatusEncomenda;
import br.com.morah.dto.StatusReserva;
import br.com.morah.repository.CobrancaRepository;
import br.com.morah.repository.ReservaRepository;
import br.com.morah.security.UsuarioLogado;
import br.com.morah.service.AvisoService;
import br.com.morah.service.CondominioService;
import br.com.morah.service.EncomendaService;
import br.com.morah.service.PortariaService;
import br.com.morah.service.UnidadeService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/** Tela inicial de morador e proprietário: tudo da própria unidade. Módulo desativado → seção vazia. */
@Component
public class DashboardMoradorMontador implements MontadorDashboard {

    private final CondominioService condominioService;
    private final UnidadeService unidadeService;
    private final AvisoService avisoService;
    private final EncomendaService encomendaService;
    private final PortariaService portariaService;
    private final CobrancaRepository cobrancaRepository;
    private final ReservaRepository reservaRepository;

    public DashboardMoradorMontador(CondominioService condominioService, UnidadeService unidadeService,
                                    AvisoService avisoService, EncomendaService encomendaService,
                                    PortariaService portariaService, CobrancaRepository cobrancaRepository,
                                    ReservaRepository reservaRepository) {
        this.condominioService = condominioService;
        this.unidadeService = unidadeService;
        this.avisoService = avisoService;
        this.encomendaService = encomendaService;
        this.portariaService = portariaService;
        this.cobrancaRepository = cobrancaRepository;
        this.reservaRepository = reservaRepository;
    }

    @Override
    public boolean atende(Perfil perfil) {
        return perfil == Perfil.MORADOR || perfil == Perfil.PROPRIETARIO;
    }

    @Override
    @Transactional(readOnly = true)
    public Dashboard montar(UsuarioLogado usuario) {
        Long condominioId = usuario.condominioId();
        Long unidadeId = usuario.unidadeId();
        PageRequest primeiros = PageRequest.of(0, LIMITE_ITENS);

        return new DashboardMorador(
                usuario.perfil(),
                ativo(condominioId, ModuloCodigo.MINHA_UNIDADE) ? unidadeService.buscarMinhaUnidade(usuario) : null,
                ativo(condominioId, ModuloCodigo.FINANCEIRO)
                        // pendentes e atrasadas (mesmo status gravado), do vencimento mais próximo
                        ? cobrancaRepository.findByUnidadeIdAndStatusOrderByVencimentoAscIdAsc(unidadeId, StatusCobranca.PENDENTE, primeiros)
                                .stream().map(br.com.morah.dto.Cobranca::de).toList()
                        : List.of(),
                ativo(condominioId, ModuloCodigo.AVISOS)
                        ? avisoService.buscarAvisos(usuario, null, null, 0, LIMITE_ITENS).content()
                        : List.of(),
                ativo(condominioId, ModuloCodigo.ENCOMENDAS)
                        ? encomendaService.buscarEncomendas(usuario, StatusEncomenda.AGUARDANDO_RETIRADA, 0, LIMITE_ITENS).content()
                        : List.of(),
                ativo(condominioId, ModuloCodigo.PORTARIA)
                        ? portariaService.buscarAutorizacoes(usuario, StatusAutorizacaoVisita.PENDENTE, 0, LIMITE_ITENS).content()
                        : List.of(),
                ativo(condominioId, ModuloCodigo.RESERVAS)
                        ? reservaRepository.findByUnidadeIdAndStatusAndInicioGreaterThanEqualOrderByInicioAsc(
                                        unidadeId, StatusReserva.CONFIRMADA, Instant.now(), primeiros)
                                .stream().map(br.com.morah.dto.Reserva::de).toList()
                        : List.of());
    }

    private boolean ativo(Long condominioId, ModuloCodigo modulo) {
        return condominioService.isModuloAtivo(condominioId, modulo);
    }
}
