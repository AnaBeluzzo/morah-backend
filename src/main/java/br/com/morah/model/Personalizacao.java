package br.com.morah.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/** Identidade visual: exatamente uma por condomínio (criada no seed). */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Personalizacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private Condominio condominio;

    private String nomeExibicao;

    private String logoUrl;

    private String corPrimaria;

    private String corSecundaria;

    private String corDestaque;

    private Instant atualizadoEm;
}
