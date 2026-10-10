package br.com.morah.pattern.strategy.rateio;

import br.com.morah.model.condominio.Unidade;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EstrategiaRateioTest {

    private Unidade unidadeComFracao(String fracao) {
        Unidade unidade = new Unidade();
        unidade.setFracaoIdeal(fracao == null ? null : new BigDecimal(fracao));
        return unidade;
    }

    @Test
    void deveTrocarOCalculoConformeAEstrategia() {
        Unidade unidade = unidadeComFracao("0.3");
        BigDecimal valorTaxa = new BigDecimal("1000");

        EstrategiaRateio estrategia = new RateioIgualitario();
        assertThat(estrategia.calcularValor(valorTaxa, unidade, 4)).isEqualByComparingTo("250.00");

        estrategia = new RateioFracaoIdeal();
        assertThat(estrategia.calcularValor(valorTaxa, unidade, 4)).isEqualByComparingTo("300.00");
    }

    @Test
    void deveArredondarEmDuasCasasHalfUp() {
        assertThat(new RateioIgualitario().calcularValor(new BigDecimal("100"), new Unidade(), 3))
                .isEqualTo(new BigDecimal("33.33"));
        assertThat(new RateioFracaoIdeal().calcularValor(new BigDecimal("10.01"), unidadeComFracao("0.5"), 2))
                .isEqualTo(new BigDecimal("5.01"));
    }

    @Test
    void deveFalharNoRateioPorFracaoQuandoUnidadeSemFracao() {
        assertThatThrownBy(() -> new RateioFracaoIdeal().calcularValor(BigDecimal.TEN, unidadeComFracao(null), 2))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
