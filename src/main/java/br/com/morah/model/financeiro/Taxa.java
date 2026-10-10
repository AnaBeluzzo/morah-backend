package br.com.morah.model.financeiro;

import br.com.morah.dto.financeiro.TipoRateio;
import br.com.morah.model.condominio.Condominio;
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

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Taxa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Condominio condominio;

    private String descricao;

    // formato AAAA-MM
    private String competencia;

    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    private TipoRateio tipoRateio;

    private Integer vencimentoDia;
}
