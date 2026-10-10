package br.com.morah.model.encomendas;

import br.com.morah.dto.encomendas.StatusAutorizacaoRetirada;
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

/** Permissão temporária para um terceiro retirar a encomenda com um código próprio. */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class AutorizacaoRetirada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Encomenda encomenda;

    private String nomeTerceiro;

    private String documento;

    private String codigo;

    private Instant validade;

    // gravado só como ativa ou utilizada
    @Enumerated(EnumType.STRING)
    private StatusAutorizacaoRetirada status;

    /** "expirada" é calculado na leitura: ativa com validade vencida. */
    public StatusAutorizacaoRetirada calcularStatus() {
        if (status == StatusAutorizacaoRetirada.ATIVA && validade.isBefore(Instant.now())) {
            return StatusAutorizacaoRetirada.EXPIRADA;
        }
        return status;
    }
}
