package br.com.morah.model.login;

import br.com.morah.dto.login.Perfil;
import br.com.morah.model.condominio.Condominio;
import br.com.morah.model.condominio.Unidade;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Vínculo de acesso: a pessoa tem o perfil X no condomínio Y (e, se for morador, na unidade Z). */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class PessoaPerfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Pessoa pessoa;

    @Enumerated(EnumType.STRING)
    private Perfil perfil;

    @ManyToOne
    private Condominio condominio;

    // nula para síndico e portaria
    @ManyToOne
    private Unidade unidade;
}
