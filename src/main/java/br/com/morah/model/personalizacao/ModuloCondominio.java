package br.com.morah.model.personalizacao;

import br.com.morah.dto.personalizacao.ModuloCodigo;
import br.com.morah.model.condominio.Condominio;
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

import java.time.Instant;

/** Uma linha por módulo ativável de cada condomínio. */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class ModuloCondominio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Condominio condominio;

    @Enumerated(EnumType.STRING)
    private ModuloCodigo modulo;

    private boolean ativo;

    private Instant atualizadoEm;
}
