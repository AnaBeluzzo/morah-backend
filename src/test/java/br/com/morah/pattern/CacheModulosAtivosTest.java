package br.com.morah.pattern;

import br.com.morah.dto.ModuloCodigo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class CacheModulosAtivosTest {

    // condomínio fictício, para não interferir nos testes de integração
    private static final Long CONDOMINIO = 999L;

    @AfterEach
    void limpar() {
        CacheModulosAtivos.INSTANCE.invalidar(CONDOMINIO);
    }

    @Test
    void deveExistirUmaUnicaInstancia() {
        assertThat(CacheModulosAtivos.INSTANCE).isSameAs(CacheModulosAtivos.valueOf("INSTANCE"));
        assertThat(CacheModulosAtivos.values()).hasSize(1);
    }

    @Test
    void deveCarregarDoBancoSoUmaVezAteSerInvalidado() {
        AtomicInteger leiturasNoBanco = new AtomicInteger();

        for (int i = 0; i < 3; i++) {
            CacheModulosAtivos.INSTANCE.buscarAtivos(CONDOMINIO, id -> {
                leiturasNoBanco.incrementAndGet();
                return EnumSet.of(ModuloCodigo.AVISOS);
            });
        }
        assertThat(leiturasNoBanco.get()).isEqualTo(1);

        CacheModulosAtivos.INSTANCE.invalidar(CONDOMINIO);
        var ativos = CacheModulosAtivos.INSTANCE.buscarAtivos(CONDOMINIO, id -> {
            leiturasNoBanco.incrementAndGet();
            return EnumSet.of(ModuloCodigo.FINANCEIRO);
        });

        assertThat(leiturasNoBanco.get()).isEqualTo(2);
        assertThat(ativos).containsExactly(ModuloCodigo.FINANCEIRO);
    }
}
