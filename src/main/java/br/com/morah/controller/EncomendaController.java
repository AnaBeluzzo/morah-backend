package br.com.morah.controller;

import br.com.morah.dto.encomendas.AutorizacaoRetirada;
import br.com.morah.dto.encomendas.AutorizacaoRetiradaCreateRequest;
import br.com.morah.dto.encomendas.Encomenda;
import br.com.morah.dto.encomendas.EncomendaCreateRequest;
import br.com.morah.dto.encomendas.EncomendaPage;
import br.com.morah.dto.encomendas.Ocorrencia;
import br.com.morah.dto.encomendas.OcorrenciaCreateRequest;
import br.com.morah.dto.encomendas.RetiradaConfirmRequest;
import br.com.morah.dto.encomendas.StatusEncomenda;
import br.com.morah.pattern.templatemethod.crud.BaseCrudController;
import br.com.morah.security.UsuarioLogado;
import br.com.morah.service.EncomendaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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

/** Encomendas. Listar, registrar e detalhar usam o fluxo fixo do BaseCrudController (Template Method). */
@RestController
@RequestMapping("/encomendas")
public class EncomendaController extends BaseCrudController<Encomenda> {

    private final EncomendaService encomendaService;

    public EncomendaController(EncomendaService encomendaService) {
        this.encomendaService = encomendaService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MORADOR', 'PROPRIETARIO', 'PORTARIA', 'SINDICO')")
    public ResponseEntity<EncomendaPage> buscarEncomendas(@RequestParam(required = false) StatusEncomenda status,
                                                          @RequestParam(defaultValue = "0") @Min(0) int page,
                                                          @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return buscarTodos(usuario -> encomendaService.buscarEncomendas(usuario, status, page, size), EncomendaPage::content);
    }

    @PostMapping
    @PreAuthorize("hasRole('PORTARIA')")
    public ResponseEntity<Encomenda> criarEncomenda(@Valid @RequestBody EncomendaCreateRequest request) {
        return criar(request, usuario -> encomendaService.criarEncomenda(usuario, request));
    }

    @GetMapping("/{encomendaId}")
    @PreAuthorize("hasAnyRole('MORADOR', 'PROPRIETARIO', 'PORTARIA', 'SINDICO')")
    public ResponseEntity<Encomenda> buscarEncomenda(@PathVariable Long encomendaId) {
        return buscar(usuario -> encomendaService.buscarEncomenda(usuario, encomendaId));
    }

    @PostMapping("/{encomendaId}/autorizacoes-retirada")
    @PreAuthorize("hasAnyRole('MORADOR', 'PROPRIETARIO')")
    @ResponseStatus(HttpStatus.CREATED)
    public AutorizacaoRetirada autorizarRetiradaTerceiro(@PathVariable Long encomendaId,
                                                         @Valid @RequestBody AutorizacaoRetiradaCreateRequest request) {
        return encomendaService.autorizarRetiradaTerceiro(UsuarioLogado.atual(), encomendaId, request);
    }

    /** retiradoPorNome/Documento são aceitos mas não gravados: a Encomenda do contrato não os expõe. */
    @PostMapping("/{encomendaId}/retirada")
    @PreAuthorize("hasRole('PORTARIA')")
    public Encomenda confirmarRetirada(@PathVariable Long encomendaId, @Valid @RequestBody RetiradaConfirmRequest request) {
        return encomendaService.confirmarRetirada(UsuarioLogado.atual(), encomendaId, request);
    }

    @PostMapping("/{encomendaId}/ocorrencias")
    @PreAuthorize("hasAnyRole('MORADOR', 'PROPRIETARIO')")
    @ResponseStatus(HttpStatus.CREATED)
    public Ocorrencia abrirOcorrencia(@PathVariable Long encomendaId, @Valid @RequestBody OcorrenciaCreateRequest request) {
        return encomendaService.abrirOcorrencia(UsuarioLogado.atual(), encomendaId, request);
    }

    /** Morador/proprietário só acessa encomendas da própria unidade (403). */
    @Override
    protected void verificarEscopo(Encomenda encomenda, UsuarioLogado usuario) {
        usuario.verificarUnidade(encomenda.unidadeId());
    }
}
