package br.com.morah.controller;

import br.com.morah.dto.portaria.Acesso;
import br.com.morah.dto.portaria.AcessoCreateRequest;
import br.com.morah.dto.portaria.AcessoPage;
import br.com.morah.dto.portaria.AutorizacaoVisita;
import br.com.morah.dto.portaria.AutorizacaoVisitaCreateRequest;
import br.com.morah.dto.portaria.AutorizacaoVisitaPage;
import br.com.morah.dto.portaria.DecisaoAutorizacaoRequest;
import br.com.morah.dto.portaria.StatusAutorizacaoVisita;
import br.com.morah.security.UsuarioLogado;
import br.com.morah.service.PortariaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/portaria")
public class PortariaController {

    private final PortariaService portariaService;

    public PortariaController(PortariaService portariaService) {
        this.portariaService = portariaService;
    }

    @PostMapping("/visitantes")
    @PreAuthorize("hasRole('PORTARIA')")
    @ResponseStatus(HttpStatus.CREATED)
    public AutorizacaoVisita registrarVisitante(@Valid @RequestBody AutorizacaoVisitaCreateRequest request) {
        return portariaService.registrarVisitante(UsuarioLogado.atual(), request);
    }

    @GetMapping("/autorizacoes")
    @PreAuthorize("hasAnyRole('PORTARIA', 'MORADOR', 'PROPRIETARIO')")
    public AutorizacaoVisitaPage buscarAutorizacoes(@RequestParam(required = false) StatusAutorizacaoVisita status,
                                                    @RequestParam(defaultValue = "0") @Min(0) int page,
                                                    @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return portariaService.buscarAutorizacoes(UsuarioLogado.atual(), status, page, size);
    }

    @GetMapping("/autorizacoes/{autorizacaoId}")
    @PreAuthorize("hasAnyRole('PORTARIA', 'MORADOR', 'PROPRIETARIO')")
    public AutorizacaoVisita buscarAutorizacao(@PathVariable Long autorizacaoId) {
        return portariaService.buscarAutorizacao(UsuarioLogado.atual(), autorizacaoId);
    }

    @PatchMapping("/autorizacoes/{autorizacaoId}/decisao")
    @PreAuthorize("hasAnyRole('MORADOR', 'PROPRIETARIO')")
    public AutorizacaoVisita decidirAutorizacao(@PathVariable Long autorizacaoId,
                                                @Valid @RequestBody DecisaoAutorizacaoRequest request) {
        return portariaService.decidirAutorizacao(UsuarioLogado.atual(), autorizacaoId, request);
    }

    @PostMapping("/autorizacoes/{autorizacaoId}/reenviar")
    @PreAuthorize("hasRole('PORTARIA')")
    public ResponseEntity<Void> reenviarAutorizacao(@PathVariable Long autorizacaoId) {
        portariaService.reenviarAutorizacao(UsuarioLogado.atual(), autorizacaoId);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/acessos")
    @PreAuthorize("hasAnyRole('PORTARIA', 'SINDICO')")
    public AcessoPage buscarAcessos(@RequestParam(required = false) LocalDate data,
                                    @RequestParam(defaultValue = "0") @Min(0) int page,
                                    @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return portariaService.buscarAcessos(UsuarioLogado.atual(), data, page, size);
    }

    /** O fotoUrl é aceito mas não gravado: o Acesso do contrato não o expõe. */
    @PostMapping("/acessos")
    @PreAuthorize("hasRole('PORTARIA')")
    @ResponseStatus(HttpStatus.CREATED)
    public Acesso registrarAcesso(@Valid @RequestBody AcessoCreateRequest request) {
        return portariaService.registrarAcesso(UsuarioLogado.atual(), request);
    }
}
