package br.com.morah.model;

import br.com.morah.dto.PrioridadeAviso;
import jakarta.persistence.Embedded;
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

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Aviso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Condominio condominio;

    private String titulo;

    private String conteudo;

    private String categoria;

    @Enumerated(EnumType.STRING)
    private PrioridadeAviso prioridade;

    @Embedded
    private PublicoAlvo publicoAlvo;

    @ManyToOne
    private Pessoa autor;

    private Instant publicadoEm;

    // nulo = sem prazo; no passado = fora de vigência
    private Instant expiraEm;

    public boolean isVigente() {
        return expiraEm == null || expiraEm.isAfter(Instant.now());
    }
}
