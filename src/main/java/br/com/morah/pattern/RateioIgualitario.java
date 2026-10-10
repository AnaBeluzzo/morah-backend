package br.com.morah.pattern;

import br.com.morah.dto.TipoRateio;
import br.com.morah.model.Unidade;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Valor da taxa dividido igualmente entre as unidades. */
@Component
public class RateioIgualitario implements EstrategiaRateio {

    @Override
    public TipoRateio getTipo() {
        return TipoRateio.IGUALITARIO;
    }

    @Override
    public BigDecimal calcularValor(BigDecimal valorTaxa, Unidade unidade, int totalUnidades) {
        return valorTaxa.divide(BigDecimal.valueOf(totalUnidades), 2, RoundingMode.HALF_UP);
    }
}
