package br.com.morah.model.portaria;

import br.com.morah.dto.portaria.StatusAcesso;
import br.com.morah.dto.portaria.TipoAcesso;
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

/** Entrada efetiva de um visitante; a saída fecha o mesmo registro. */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Acesso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // opcional: entrada pode acontecer sem autorização prévia
    @ManyToOne
    private AutorizacaoVisita autorizacao;

    @ManyToOne
    private Visitante visitante;

    private Instant entrada;

    private Instant saida;

    @Enumerated(EnumType.STRING)
    private TipoAcesso tipo;

    @Enumerated(EnumType.STRING)
    private StatusAcesso status;
}
