package br.com.morah.pattern;

import br.com.morah.dto.Perfil;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MontadorDashboardTest {

    // as dependências não são usadas por atende(), então podem ser nulas aqui
    private final List<MontadorDashboard> montadores = List.of(
            new DashboardMoradorMontador(null, null, null, null, null, null, null),
            new DashboardSindicoMontador(null, null, null, null, null),
            new DashboardPortariaMontador(null, null, null, null));

    private MontadorDashboard escolher(Perfil perfil) {
        return montadores.stream().filter(m -> m.atende(perfil)).findFirst().orElseThrow();
    }

    @Test
    void deveTrocarAEstrategiaConformeOPerfil() {
        assertThat(escolher(Perfil.MORADOR)).isInstanceOf(DashboardMoradorMontador.class);
        assertThat(escolher(Perfil.PROPRIETARIO)).isInstanceOf(DashboardMoradorMontador.class);
        assertThat(escolher(Perfil.SINDICO)).isInstanceOf(DashboardSindicoMontador.class);
        assertThat(escolher(Perfil.PORTARIA)).isInstanceOf(DashboardPortariaMontador.class);
    }

    @Test
    void deveExistirExatamenteUmMontadorParaCadaPerfil() {
        for (Perfil perfil : Perfil.values()) {
            assertThat(montadores.stream().filter(m -> m.atende(perfil)).count()).isEqualTo(1);
        }
    }
}
