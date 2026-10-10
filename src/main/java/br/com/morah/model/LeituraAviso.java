package br.com.morah.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/** Registro de que a pessoa abriu o aviso (alimenta o campo lidoPeloUsuario). */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class LeituraAviso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Aviso aviso;

    @ManyToOne
    private Pessoa pessoa;

    private Instant lidoEm;
}
