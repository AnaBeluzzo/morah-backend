package br.com.morah.model;

import br.com.morah.dto.TipoNotificacao;
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
public class Notificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // destinatário
    @ManyToOne
    private Pessoa pessoa;

    @Enumerated(EnumType.STRING)
    private TipoNotificacao tipo;

    private String titulo;

    private String mensagem;

    // id do recurso que gerou a notificação (aviso, encomenda, cobrança...)
    private Long referenciaId;

    private Instant enviadaEm;

    private Instant lidaEm;
}
