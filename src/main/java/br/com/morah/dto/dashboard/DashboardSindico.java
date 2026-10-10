package br.com.morah.dto.dashboard;

import br.com.morah.dto.avisos.Aviso;
import br.com.morah.dto.financeiro.InadimplenciaResumo;
import br.com.morah.dto.login.Perfil;

import java.util.List;

public record DashboardSindico(
        Perfil perfil,
        InadimplenciaResumo inadimplencia,
        List<Aviso> avisosPublicadosRecentes,
        long solicitacoesVinculoPendentes,
        long ocorrenciasAbertas) implements Dashboard {
}
