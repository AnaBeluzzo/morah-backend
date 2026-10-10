package br.com.morah.model.reservas;

import br.com.morah.dto.reservas.StatusReserva;
import br.com.morah.model.condominio.Unidade;
import br.com.morah.model.login.Pessoa;
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
import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private AreaComum areaComum;

    @ManyToOne
    private Unidade unidade;

    @ManyToOne
    private Pessoa solicitante;

    private Instant inicio;

    private Instant fim;

    @Enumerated(EnumType.STRING)
    private StatusReserva status;

    private BigDecimal valor;
}
