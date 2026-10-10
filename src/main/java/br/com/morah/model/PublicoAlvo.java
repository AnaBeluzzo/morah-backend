package br.com.morah.model;

import br.com.morah.dto.TipoPublicoAlvo;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Para quem o aviso é destinado. Fica embutido na tabela aviso (colunas publico_*). */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class PublicoAlvo {

    @Enumerated(EnumType.STRING)
    @Column(name = "publico_tipo")
    private TipoPublicoAlvo tipo;

    @Column(name = "publico_bloco_id")
    private Long blocoId;

    @Column(name = "publico_unidade_id")
    private Long unidadeId;
}
