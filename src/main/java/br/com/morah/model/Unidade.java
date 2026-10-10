package br.com.morah.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** A unidade pertence a um bloco; o condomínio da unidade é o condomínio do bloco. */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Unidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Bloco bloco;

    private String identificacao;

    private String tipo;

    private BigDecimal areaM2;

    private BigDecimal fracaoIdeal;

    private String status;
}
