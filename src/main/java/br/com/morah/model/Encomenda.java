package br.com.morah.model;

import br.com.morah.dto.StatusEncomenda;
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
public class Encomenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Unidade unidade;

    private String remetente;

    private String transportadora;

    private String codigoRastreio;

    // código de 6 dígitos que o morador apresenta na portaria
    private String codigoRetirada;

    private String fotoUrl;

    private Instant recebidaEm;

    private Instant retiradaEm;

    @Enumerated(EnumType.STRING)
    private StatusEncomenda status;
}
