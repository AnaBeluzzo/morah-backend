package br.com.morah.model;

import br.com.morah.dto.StatusCobranca;
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

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Cobranca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Unidade unidade;

    @ManyToOne
    private Taxa taxa;

    private String competencia;

    private LocalDate vencimento;

    private BigDecimal valorOriginal;

    private BigDecimal multa;

    private BigDecimal juros;

    // gravado só como pendente, pago ou cancelado
    @Enumerated(EnumType.STRING)
    private StatusCobranca status;

    /** "atrasado" é calculado na leitura: pendente com vencimento antes de hoje (sem job agendado). */
    public StatusCobranca calcularStatus() {
        if (status == StatusCobranca.PENDENTE && vencimento.isBefore(LocalDate.now())) {
            return StatusCobranca.ATRASADO;
        }
        return status;
    }

    public BigDecimal calcularValorTotal() {
        return valorOriginal.add(multa).add(juros);
    }
}
