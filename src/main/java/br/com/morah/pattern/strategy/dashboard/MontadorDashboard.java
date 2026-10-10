package br.com.morah.pattern.strategy.dashboard;

import br.com.morah.dto.dashboard.Dashboard;
import br.com.morah.dto.login.Perfil;
import br.com.morah.security.UsuarioLogado;

/**
 * Strategy.
 * Problema que resolve: cada perfil tem uma tela inicial diferente; o controller só escolhe
 * o montador que atende o perfil ativo, sem um if/else gigante com as três montagens.
 */
public interface MontadorDashboard {

    /** Listas de "recentes"/"próximos" trazem no máximo 5 itens. */
    int LIMITE_ITENS = 5;

    boolean atende(Perfil perfil);

    Dashboard montar(UsuarioLogado usuario);
}
