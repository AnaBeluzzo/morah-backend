package br.com.morah.controller;

import br.com.morah.dto.ContextoAtivoResponse;
import br.com.morah.dto.ContextoRequest;
import br.com.morah.dto.LoginRequest;
import br.com.morah.dto.LoginResponse;
import br.com.morah.dto.MeResponse;
import br.com.morah.dto.RefreshRequest;
import br.com.morah.security.UsuarioLogado;
import br.com.morah.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('MORADOR', 'PROPRIETARIO', 'SINDICO', 'PORTARIA')")
    public MeResponse buscarMe() {
        return authService.buscarMe(UsuarioLogado.atual());
    }

    @PostMapping("/contexto")
    @PreAuthorize("hasAnyRole('MORADOR', 'PROPRIETARIO', 'SINDICO', 'PORTARIA')")
    public ContextoAtivoResponse selecionarContexto(@Valid @RequestBody ContextoRequest request) {
        return authService.selecionarContexto(UsuarioLogado.atual(), request);
    }

    @PostMapping("/refresh")
    public ContextoAtivoResponse renovarToken(@Valid @RequestBody RefreshRequest request) {
        return authService.renovarToken(request);
    }

    @PostMapping("/logout")
    @PreAuthorize("hasAnyRole('MORADOR', 'PROPRIETARIO', 'SINDICO', 'PORTARIA')")
    public ResponseEntity<Void> logout() {
        authService.logout(UsuarioLogado.atual());
        return ResponseEntity.noContent().build();
    }
}
