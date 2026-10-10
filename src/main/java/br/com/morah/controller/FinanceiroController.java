package br.com.morah.controller;

import br.com.morah.dto.BaixaManualRequest;
import br.com.morah.dto.Cobranca;
import br.com.morah.dto.CobrancaPage;
import br.com.morah.dto.DocumentoFinanceiro;
import br.com.morah.dto.DocumentoFinanceiroCreateRequest;
import br.com.morah.dto.DocumentoFinanceiroPage;
import br.com.morah.dto.GerarCobrancasResponse;
import br.com.morah.dto.InadimplenciaResumo;
import br.com.morah.dto.PixResponse;
import br.com.morah.dto.StatusCobranca;
import br.com.morah.dto.Taxa;
import br.com.morah.dto.TaxaCreateRequest;
import br.com.morah.security.UsuarioLogado;
import br.com.morah.service.FinanceiroService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/financeiro")
public class FinanceiroController {

    private final FinanceiroService financeiroService;

    public FinanceiroController(FinanceiroService financeiroService) {
        this.financeiroService = financeiroService;
    }

    @GetMapping("/cobrancas")
    @PreAuthorize("hasAnyRole('MORADOR', 'PROPRIETARIO', 'SINDICO')")
    public CobrancaPage buscarCobrancas(@RequestParam(required = false) StatusCobranca status,
                                       @RequestParam(required = false) @Pattern(regexp = "^\\d{4}-\\d{2}$") String competencia,
                                       @RequestParam(required = false) Long unidadeId,
                                       @RequestParam(defaultValue = "0") @Min(0) int page,
                                       @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return financeiroService.buscarCobrancas(UsuarioLogado.atual(), status, competencia, unidadeId, page, size);
    }

    @GetMapping("/cobrancas/{cobrancaId}")
    @PreAuthorize("hasAnyRole('MORADOR', 'PROPRIETARIO', 'SINDICO')")
    public Cobranca buscarCobranca(@PathVariable Long cobrancaId) {
        return financeiroService.buscarCobranca(UsuarioLogado.atual(), cobrancaId);
    }

    @GetMapping("/cobrancas/{cobrancaId}/pix")
    @PreAuthorize("hasAnyRole('MORADOR', 'PROPRIETARIO')")
    public PixResponse gerarPix(@PathVariable Long cobrancaId) {
        return financeiroService.gerarPix(UsuarioLogado.atual(), cobrancaId);
    }

    /** Meio, valor, data e observação são validados mas não gravados: a Cobranca do contrato não os expõe. */
    @PostMapping("/cobrancas/{cobrancaId}/baixa-manual")
    @PreAuthorize("hasRole('SINDICO')")
    public Cobranca baixarCobrancaManualmente(@PathVariable Long cobrancaId,
                                              @Valid @RequestBody BaixaManualRequest request) {
        return financeiroService.baixarCobrancaManualmente(cobrancaId);
    }

    /** Rota pública (chamada pelo gateway): o corpo chega bruto para conferir a assinatura HMAC. */
    @PostMapping("/webhooks/pagamentos")
    public ResponseEntity<Void> receberWebhookPagamento(HttpEntity<String> requisicao) {
        financeiroService.receberWebhookPagamento(requisicao);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/documentos")
    @PreAuthorize("hasAnyRole('MORADOR', 'PROPRIETARIO', 'SINDICO')")
    public DocumentoFinanceiroPage buscarDocumentosFinanceiros(@RequestParam(defaultValue = "0") @Min(0) int page,
                                                               @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return financeiroService.buscarDocumentosFinanceiros(UsuarioLogado.atual(), page, size);
    }

    @PostMapping("/documentos")
    @PreAuthorize("hasRole('SINDICO')")
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentoFinanceiro criarDocumentoFinanceiro(@Valid @RequestBody DocumentoFinanceiroCreateRequest request) {
        return financeiroService.criarDocumentoFinanceiro(UsuarioLogado.atual(), request);
    }

    @PostMapping("/taxas")
    @PreAuthorize("hasRole('SINDICO')")
    @ResponseStatus(HttpStatus.CREATED)
    public Taxa criarTaxa(@Valid @RequestBody TaxaCreateRequest request) {
        return financeiroService.criarTaxa(UsuarioLogado.atual(), request);
    }

    @PostMapping("/taxas/{taxaId}/gerar-cobrancas")
    @PreAuthorize("hasRole('SINDICO')")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public GerarCobrancasResponse gerarCobrancasEmLote(@PathVariable Long taxaId) {
        return financeiroService.gerarCobrancasEmLote(UsuarioLogado.atual(), taxaId);
    }

    @GetMapping("/inadimplencia")
    @PreAuthorize("hasRole('SINDICO')")
    public InadimplenciaResumo buscarResumoInadimplencia() {
        return financeiroService.buscarResumoInadimplencia(UsuarioLogado.atual().condominioId());
    }
}
