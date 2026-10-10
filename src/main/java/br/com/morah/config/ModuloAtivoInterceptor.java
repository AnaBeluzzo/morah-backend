package br.com.morah.config;

import br.com.morah.dto.ModuloCodigo;
import br.com.morah.exception.ApiException;
import br.com.morah.security.UsuarioLogado;
import br.com.morah.service.CondominioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

/**
 * Bloqueia (403) as rotas de um módulo desativado no condomínio do token, para qualquer perfil.
 * Login, Tela Inicial, Notificações e Personalização não estão no mapa: nunca são bloqueados.
 */
@Component
public class ModuloAtivoInterceptor implements HandlerInterceptor {

    private static final Map<String, ModuloCodigo> MODULO_POR_PREFIXO = Map.of(
            "/financeiro/", ModuloCodigo.FINANCEIRO,
            "/portaria/", ModuloCodigo.PORTARIA,
            "/avisos", ModuloCodigo.AVISOS,
            "/areas-comuns", ModuloCodigo.RESERVAS,
            "/reservas", ModuloCodigo.RESERVAS,
            "/unidades/", ModuloCodigo.MINHA_UNIDADE,
            "/encomendas", ModuloCodigo.ENCOMENDAS);

    // chamado pelo gateway, não pelo app: nunca é bloqueado
    private static final String ROTA_WEBHOOK = "/financeiro/webhooks/pagamentos";

    private final CondominioService condominioService;

    public ModuloAtivoInterceptor(CondominioService condominioService) {
        this.condominioService = condominioService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String rota = request.getRequestURI().substring(request.getContextPath().length());
        boolean autenticado = SecurityContextHolder.getContext().getAuthentication() instanceof JwtAuthenticationToken;
        if (rota.equals(ROTA_WEBHOOK) || !autenticado) {
            return true;
        }

        for (Map.Entry<String, ModuloCodigo> entrada : MODULO_POR_PREFIXO.entrySet()) {
            ModuloCodigo modulo = entrada.getValue();
            if (rota.startsWith(entrada.getKey())
                    && !condominioService.isModuloAtivo(UsuarioLogado.atual().condominioId(), modulo)) {
                throw ApiException.proibido("Módulo " + modulo.getNome() + " desativado neste condomínio");
            }
        }
        return true;
    }
}
