package br.com.morah.dto;

import java.util.List;

public record DashboardSindico(
        Perfil perfil,
        InadimplenciaResumo inadimplencia,
        List<Aviso> avisosPublicadosRecentes,
        long solicitacoesVinculoPendentes,
        long ocorrenciasAbertas) implements Dashboard {
}
