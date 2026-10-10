package br.com.morah.pattern;

import br.com.morah.dto.TipoRateio;
import br.com.morah.model.Unidade;

import java.math.BigDecimal;

/**
 * Strategy.
 * Problema que resolve: o valor de cada unidade muda conforme o tipo de rateio da taxa;
 * a geração em lote só escolhe a estratégia pelo tipoRateio e chama calcularValor().
 */
public interface EstrategiaRateio {

    TipoRateio getTipo();

    /** Lança IllegalArgumentException quando a unidade não pode ser cobrada (vira uma "falha"). */
    BigDecimal calcularValor(BigDecimal valorTaxa, Unidade unidade, int totalUnidades);
}
