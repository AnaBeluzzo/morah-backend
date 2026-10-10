package br.com.morah.pattern.singleton;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GeradorCodigoTest {

    @Test
    void deveDevolverSempreAMesmaInstancia() {
        assertThat(GeradorCodigo.getInstance()).isSameAs(GeradorCodigo.getInstance());
    }

    @Test
    void deveGerarCodigoDeSeisDigitos() {
        for (int i = 0; i < 100; i++) {
            assertThat(GeradorCodigo.getInstance().gerarCodigo()).matches("\\d{6}");
        }
    }
}
