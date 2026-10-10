package br.com.morah.pattern.strategy.dashboard;

import br.com.morah.dto.dashboard.Dashboard;
import br.com.morah.dto.dashboard.DashboardSindico;
import br.com.morah.dto.encomendas.StatusOcorrencia;
import br.com.morah.dto.login.Perfil;
import br.com.morah.dto.minhaunidade.StatusSolicitacaoVinculo;
import br.com.morah.dto.personalizacao.ModuloCodigo;
import br.com.morah.repository.encomendas.OcorrenciaRepository;
import br.com.morah.repository.minhaunidade.SolicitacaoVinculoRepository;
import br.com.morah.security.UsuarioLogado;
import br.com.morah.service.AvisoService;
import br.com.morah.service.CondominioService;
import br.com.morah.service.FinanceiroService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Tela inicial do síndico: indicadores do condomínio. Financeiro desativado → sem "inadimplencia". */
@Component
public class DashboardSindicoMontador implements MontadorDashboard {

    private final CondominioService condominioService;
    private final FinanceiroService financeiroService;
    private final AvisoService avisoService;
    private final SolicitacaoVinculoRepository solicitacaoVinculoRepository;
    private final OcorrenciaRepository ocorrenciaRepository;

    public DashboardSindicoMontador(CondominioService condominioService, FinanceiroService financeiroService,
                                    AvisoService avisoService, SolicitacaoVinculoRepository solicitacaoVinculoRepository,
                                    OcorrenciaRepository ocorrenciaRepository) {
        this.condominioService = condominioService;
        this.financeiroService = financeiroService;
        this.avisoService = avisoService;
        this.solicitacaoVinculoRepository = solicitacaoVinculoRepository;
        this.ocorrenciaRepository = ocorrenciaRepository;
    }

    @Override
    public boolean atende(Perfil perfil) {
        return perfil == Perfil.SINDICO;
    }

    @Override
    @Transactional(readOnly = true)
    public Dashboard montar(UsuarioLogado usuario) {
        Long condominioId = usuario.condominioId();
        return new DashboardSindico(
                usuario.perfil(),
                ativo(condominioId, ModuloCodigo.FINANCEIRO) ? financeiroService.buscarResumoInadimplencia(condominioId) : null,
                ativo(condominioId, ModuloCodigo.AVISOS)
                        ? avisoService.buscarAvisos(usuario, null, null, 0, LIMITE_ITENS).content()
                        : List.of(),
                ativo(condominioId, ModuloCodigo.MINHA_UNIDADE)
                        ? solicitacaoVinculoRepository.countByUnidadeBlocoCondominioIdAndStatus(condominioId, StatusSolicitacaoVinculo.PENDENTE)
                        : 0,
                // ocorrências vêm de reservas e de encomendas: não pertencem a um módulo só
                ocorrenciaRepository.countByCondominioIdAndStatus(condominioId, StatusOcorrencia.ABERTA));
    }

    private boolean ativo(Long condominioId, ModuloCodigo modulo) {
        return condominioService.isModuloAtivo(condominioId, modulo);
    }
}
