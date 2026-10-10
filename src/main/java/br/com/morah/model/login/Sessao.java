package br.com.morah.model.login;

import br.com.morah.dto.login.Perfil;
import br.com.morah.model.condominio.Condominio;
import br.com.morah.model.condominio.Unidade;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Sessão criada a cada login. O access token carrega o id dela (claim "sid"):
 * apagar a sessão invalida o token na hora (logout).
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Sessao {

    @Id
    private String id;

    @ManyToOne
    private Pessoa pessoa;

    @Enumerated(EnumType.STRING)
    private Perfil perfil;

    @ManyToOne
    private Condominio condominio;

    @ManyToOne
    private Unidade unidade;

    private String refreshToken;

    private Instant refreshExpiraEm;
}
