package br.com.morah.controller;

import br.com.morah.dto.Dashboard;
import br.com.morah.pattern.MontadorDashboard;
import br.com.morah.security.UsuarioLogado;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DashboardController {

    // o Spring injeta todas as estratégias (um montador por formato de tela)
    private final List<MontadorDashboard> montadores;

    public DashboardController(List<MontadorDashboard> montadores) {
        this.montadores = montadores;
    }

    @GetMapping("/home/dashboard")
    @PreAuthorize("hasAnyRole('MORADOR', 'PROPRIETARIO', 'SINDICO', 'PORTARIA')")
    public Dashboard buscarDashboard() {
        UsuarioLogado usuario = UsuarioLogado.atual();
        // Strategy: escolhe o montador pelo perfil ativo
        MontadorDashboard montador = montadores.stream()
                .filter(m -> m.atende(usuario.perfil()))
                .findFirst()
                .orElseThrow();
        return montador.montar(usuario);
    }
}
