package br.com.morah.model.encomendas;

import br.com.morah.dto.encomendas.StatusOcorrencia;
import br.com.morah.model.condominio.Condominio;
import br.com.morah.model.reservas.Reserva;
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

/** Só é criada em dois pontos do contrato: vistoria com dano e extravio de encomenda. */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Ocorrencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Condominio condominio;

    // preenchida quando a ocorrência vem de uma vistoria
    @ManyToOne
    private Reserva reserva;

    // preenchida quando a ocorrência é de extravio de encomenda
    @ManyToOne
    private Encomenda encomenda;

    private String titulo;

    private String descricao;

    @Enumerated(EnumType.STRING)
    private StatusOcorrencia status;

    private Instant abertaEm;

    private Instant encerradaEm;

    /** Ocorrência aberta agora; quem chama liga à reserva ou à encomenda de origem. */
    public static Ocorrencia aberta(Condominio condominio, String titulo, String descricao) {
        Ocorrencia ocorrencia = new Ocorrencia();
        ocorrencia.setCondominio(condominio);
        ocorrencia.setTitulo(titulo);
        ocorrencia.setDescricao(descricao);
        ocorrencia.setStatus(StatusOcorrencia.ABERTA);
        ocorrencia.setAbertaEm(Instant.now());
        return ocorrencia;
    }
}
