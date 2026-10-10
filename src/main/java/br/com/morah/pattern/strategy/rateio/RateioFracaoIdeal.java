package br.com.morah.pattern.strategy.rateio;

import br.com.morah.dto.financeiro.TipoRateio;
import br.com.morah.model.condominio.Unidade;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Valor da taxa multiplicado pela fração ideal da unidade. */
@Component
public class RateioFracaoIdeal implements EstrategiaRateio {

    @Override
    public TipoRateio getTipo() {
        return TipoRateio.FRACAO_IDEAL;
    }

    @Override
    public BigDecimal calcularValor(BigDecimal valorTaxa, Unidade unidade, int totalUnidades) {
        if (unidade.getFracaoIdeal() == null) {
            throw new IllegalArgumentException("Unidade sem fração ideal cadastrada");
        }
        return valorTaxa.multiply(unidade.getFracaoIdeal()).setScale(2, RoundingMode.HALF_UP);
    }
}
