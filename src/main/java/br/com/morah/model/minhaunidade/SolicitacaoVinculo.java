package br.com.morah.model.minhaunidade;

import br.com.morah.dto.minhaunidade.StatusSolicitacaoVinculo;
import br.com.morah.dto.minhaunidade.TipoVinculo;
import br.com.morah.model.condominio.Unidade;
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

/** Pedido do morador para incluir alguém na unidade; o síndico aprova ou rejeita. */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class SolicitacaoVinculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Unidade unidade;

    @Enumerated(EnumType.STRING)
    private TipoVinculo tipoVinculo;

    private String nomeSolicitado;

    // guardado para criar a pessoa quando a solicitação for aprovada
    private String cpf;

    @Enumerated(EnumType.STRING)
    private StatusSolicitacaoVinculo status;

    private Instant solicitadoEm;
}
