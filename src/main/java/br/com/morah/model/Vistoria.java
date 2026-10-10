package br.com.morah.model;

import br.com.morah.dto.MomentoVistoria;
import jakarta.persistence.ElementCollection;
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
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Vistoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Reserva reserva;

    @Enumerated(EnumType.STRING)
    private MomentoVistoria momento;

    private String observacao;

    private boolean dano;

    // lista simples de URLs, gravada numa tabela auxiliar (vistoria_fotos_url)
    @ElementCollection
    private List<String> fotosUrl = new ArrayList<>();

    private Instant registradaEm;
}
