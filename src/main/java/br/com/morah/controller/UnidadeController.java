package br.com.morah.controller;

import br.com.morah.dto.DecisaoSolicitacaoVinculoRequest;
import br.com.morah.dto.InquilinoCreateRequest;
import br.com.morah.dto.SolicitacaoVinculo;
import br.com.morah.dto.SolicitacaoVinculoCreateRequest;
import br.com.morah.dto.Unidade;
import br.com.morah.dto.VinculoUnidade;
import br.com.morah.security.UsuarioLogado;
import br.com.morah.service.UnidadeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/unidades")
public class UnidadeController {

    private final UnidadeService unidadeService;

    public UnidadeController(UnidadeService unidadeService) {
        this.unidadeService = unidadeService;
    }

    @GetMapping("/minha")
    @PreAuthorize("hasAnyRole('MORADOR', 'PROPRIETARIO')")
    public Unidade buscarMinhaUnidade() {
        return unidadeService.buscarMinhaUnidade(UsuarioLogado.atual());
    }

    @GetMapping("/minha/vinculos")
    @PreAuthorize("hasAnyRole('MORADOR', 'PROPRIETARIO')")
    public List<VinculoUnidade> buscarVinculos() {
        return unidadeService.buscarVinculos(UsuarioLogado.atual());
    }

    @PostMapping("/minha/vinculos/solicitacoes")
    @PreAuthorize("hasAnyRole('MORADOR', 'PROPRIETARIO')")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public SolicitacaoVinculo solicitarVinculo(@Valid @RequestBody SolicitacaoVinculoCreateRequest request) {
        return unidadeService.solicitarVinculo(UsuarioLogado.atual(), request);
    }

    @PostMapping("/minha/inquilinos")
    @PreAuthorize("hasRole('PROPRIETARIO')")
    @ResponseStatus(HttpStatus.CREATED)
    public VinculoUnidade criarInquilino(@Valid @RequestBody InquilinoCreateRequest request) {
        return unidadeService.criarInquilino(UsuarioLogado.atual(), request);
    }

    @DeleteMapping("/minha/inquilinos/{vinculoId}")
    @PreAuthorize("hasRole('PROPRIETARIO')")
    public ResponseEntity<Void> deletarInquilino(@PathVariable Long vinculoId) {
        unidadeService.deletarInquilino(UsuarioLogado.atual(), vinculoId);
        return ResponseEntity.noContent().build();
    }

    /** Uso administrativo do síndico: a unidade vem da URL, mas só dentro do condomínio ativo. */
    @GetMapping("/{unidadeId}")
    @PreAuthorize("hasRole('SINDICO')")
    public Unidade buscarUnidade(@PathVariable Long unidadeId) {
        return unidadeService.buscarUnidade(UsuarioLogado.atual(), unidadeId);
    }

    @GetMapping("/{unidadeId}/vinculos/solicitacoes")
    @PreAuthorize("hasRole('SINDICO')")
    public List<SolicitacaoVinculo> buscarSolicitacoesVinculo(@PathVariable Long unidadeId) {
        return unidadeService.buscarSolicitacoesVinculo(UsuarioLogado.atual(), unidadeId);
    }

    @PatchMapping("/{unidadeId}/vinculos/solicitacoes/{solicitacaoId}")
    @PreAuthorize("hasRole('SINDICO')")
    public SolicitacaoVinculo decidirSolicitacaoVinculo(@PathVariable Long unidadeId, @PathVariable Long solicitacaoId,
                                                        @Valid @RequestBody DecisaoSolicitacaoVinculoRequest request) {
        return unidadeService.decidirSolicitacaoVinculo(UsuarioLogado.atual(), unidadeId, solicitacaoId, request);
    }
}
