package br.com.morah.controller;

import br.com.morah.dto.Aviso;
import br.com.morah.dto.AvisoCreateRequest;
import br.com.morah.dto.AvisoPage;
import br.com.morah.dto.AvisoUpdateRequest;
import br.com.morah.dto.PrioridadeAviso;
import br.com.morah.dto.PublicoAlvo;
import br.com.morah.dto.TipoPublicoAlvo;
import br.com.morah.exception.ApiException;
import br.com.morah.pattern.BaseCrudController;
import br.com.morah.security.UsuarioLogado;
import br.com.morah.service.AvisoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** CRUD completo de avisos usando o fluxo fixo do BaseCrudController (Template Method). */
@RestController
public class AvisoController extends BaseCrudController<Aviso> {

    private final AvisoService avisoService;

    public AvisoController(AvisoService avisoService) {
        this.avisoService = avisoService;
    }

    @GetMapping("/avisos")
    @PreAuthorize("hasAnyRole('MORADOR', 'PROPRIETARIO', 'SINDICO', 'PORTARIA')")
    public ResponseEntity<AvisoPage> buscarAvisos(@RequestParam(required = false) String categoria,
                                                  @RequestParam(required = false) PrioridadeAviso prioridade,
                                                  @RequestParam(defaultValue = "0") @Min(0) int page,
                                                  @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return buscarTodos(usuario -> avisoService.buscarAvisos(usuario, categoria, prioridade, page, size),
                AvisoPage::content);
    }

    @PostMapping("/avisos")
    @PreAuthorize("hasRole('SINDICO')")
    public ResponseEntity<Aviso> criarAviso(@Valid @RequestBody AvisoCreateRequest request) {
        return criar(request, usuario -> avisoService.criarAviso(usuario, request));
    }

    @GetMapping("/avisos/{avisoId}")
    @PreAuthorize("hasAnyRole('MORADOR', 'PROPRIETARIO', 'SINDICO', 'PORTARIA')")
    public ResponseEntity<Aviso> buscarAviso(@PathVariable Long avisoId) {
        return buscar(usuario -> avisoService.buscarAviso(usuario, avisoId));
    }

    @PatchMapping("/avisos/{avisoId}")
    @PreAuthorize("hasRole('SINDICO')")
    public ResponseEntity<Aviso> alterarAviso(@PathVariable Long avisoId, @Valid @RequestBody AvisoUpdateRequest request) {
        return alterar(request, usuario -> avisoService.alterarAviso(usuario, avisoId, request));
    }

    @DeleteMapping("/avisos/{avisoId}")
    @PreAuthorize("hasRole('SINDICO')")
    public ResponseEntity<Void> deletarAviso(@PathVariable Long avisoId) {
        return deletar(usuario -> avisoService.deletarAviso(usuario, avisoId));
    }

    /** Público "bloco" exige blocoId e "unidade" exige unidadeId. */
    @Override
    protected void validar(Object input) {
        if (input instanceof AvisoCreateRequest request) {
            PublicoAlvo publico = request.publicoAlvo();
            if (publico.tipo() == TipoPublicoAlvo.BLOCO && publico.blocoId() == null) {
                throw ApiException.requisicaoInvalida("publicoAlvo.blocoId é obrigatório para o público 'bloco'");
            }
            if (publico.tipo() == TipoPublicoAlvo.UNIDADE && publico.unidadeId() == null) {
                throw ApiException.requisicaoInvalida("publicoAlvo.unidadeId é obrigatório para o público 'unidade'");
            }
        }
    }

    /** Morador/proprietário fora do público-alvo do aviso → 403. */
    @Override
    protected void verificarEscopo(Aviso aviso, UsuarioLogado usuario) {
        if (!avisoService.isVisivel(aviso.publicoAlvo(), usuario)) {
            throw ApiException.proibido("Este aviso não é destinado à sua unidade");
        }
    }
}
