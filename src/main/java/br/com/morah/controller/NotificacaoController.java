package br.com.morah.controller;

import br.com.morah.dto.notificacoes.Notificacao;
import br.com.morah.security.UsuarioLogado;
import br.com.morah.service.NotificacaoService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class NotificacaoController {

    private final NotificacaoService notificacaoService;

    public NotificacaoController(NotificacaoService notificacaoService) {
        this.notificacaoService = notificacaoService;
    }

    @GetMapping("/notificacoes")
    @PreAuthorize("hasAnyRole('MORADOR', 'PROPRIETARIO', 'SINDICO', 'PORTARIA')")
    public List<Notificacao> buscarNotificacoes(@RequestParam(defaultValue = "0") @Min(0) int page,
                                                @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return notificacaoService.buscarNotificacoes(UsuarioLogado.atual(), page, size);
    }

    @PatchMapping("/notificacoes/{notificacaoId}/lida")
    @PreAuthorize("hasAnyRole('MORADOR', 'PROPRIETARIO', 'SINDICO', 'PORTARIA')")
    public Notificacao marcarNotificacaoLida(@PathVariable Long notificacaoId) {
        return notificacaoService.marcarNotificacaoLida(UsuarioLogado.atual(), notificacaoId);
    }
}
