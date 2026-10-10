package br.com.morah.pattern.strategy.dashboard;

import br.com.morah.dto.dashboard.Dashboard;
import br.com.morah.dto.dashboard.DashboardPortaria;
import br.com.morah.dto.login.Perfil;
import br.com.morah.dto.personalizacao.ModuloCodigo;
import br.com.morah.dto.portaria.StatusAutorizacaoVisita;
import br.com.morah.repository.encomendas.EncomendaRepository;
import br.com.morah.security.UsuarioLogado;
import br.com.morah.service.CondominioService;
import br.com.morah.service.PortariaService;
import br.com.morah.service.ReservaService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/** Tela inicial da portaria: fila da guarita, reservas do dia e encomendas recebidas hoje. */
@Component
public class DashboardPortariaMontador implements MontadorDashboard {

    private final CondominioService condominioService;
    private final PortariaService portariaService;
    private final ReservaService reservaService;
    private final EncomendaRepository encomendaRepository;

    public DashboardPortariaMontador(CondominioService condominioService, PortariaService portariaService,
                                     ReservaService reservaService, EncomendaRepository encomendaRepository) {
        this.condominioService = condominioService;
        this.portariaService = portariaService;
        this.reservaService = reservaService;
        this.encomendaRepository = encomendaRepository;
    }

    @Override
    public boolean atende(Perfil perfil) {
        return perfil == Perfil.PORTARIA;
    }

    @Override
    @Transactional(readOnly = true)
    public Dashboard montar(UsuarioLogado usuario) {
        Long condominioId = usuario.condominioId();
        LocalDate hoje = LocalDate.now();
        return new DashboardPortaria(
                usuario.perfil(),
                ativo(condominioId, ModuloCodigo.PORTARIA)
                        ? portariaService.buscarAutorizacoes(usuario, StatusAutorizacaoVisita.PENDENTE, 0, LIMITE_ITENS).content()
                        : List.of(),
                ativo(condominioId, ModuloCodigo.RESERVAS)
                        ? reservaService.buscarReservas(usuario, hoje, 0, LIMITE_ITENS).content()
                        : List.of(),
                ativo(condominioId, ModuloCodigo.ENCOMENDAS)
                        ? encomendaRepository.countByUnidadeBlocoCondominioIdAndRecebidaEmGreaterThanEqual(
                                condominioId, hoje.atStartOfDay(ZoneId.systemDefault()).toInstant())
                        : 0);
    }

    private boolean ativo(Long condominioId, ModuloCodigo modulo) {
        return condominioService.isModuloAtivo(condominioId, modulo);
    }
}
