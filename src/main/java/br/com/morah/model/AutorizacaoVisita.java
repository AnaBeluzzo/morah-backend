package br.com.morah.model;

import br.com.morah.dto.StatusAutorizacaoVisita;
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

import java.time.Duration;
import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class AutorizacaoVisita {

    public static final int MINUTOS_EXPIRACAO_AUTORIZACAO = 30;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Visitante visitante;

    @ManyToOne
    private Unidade unidade;

    private String motivo;

    private Instant solicitadoEm;

    private Instant respondidoEm;

    // gravado só como pendente, autorizada ou recusada
    @Enumerated(EnumType.STRING)
    private StatusAutorizacaoVisita status;

    /** "expirada" é calculado na leitura: pendente sem resposta há mais de 30 minutos. */
    public StatusAutorizacaoVisita calcularStatus() {
        Instant limite = solicitadoEm.plus(Duration.ofMinutes(MINUTOS_EXPIRACAO_AUTORIZACAO));
        if (status == StatusAutorizacaoVisita.PENDENTE && limite.isBefore(Instant.now())) {
            return StatusAutorizacaoVisita.EXPIRADA;
        }
        return status;
    }
}
