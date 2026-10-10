package br.com.morah.dto;

import java.time.Instant;

public record SolicitacaoVinculo(
        Long id,
        Long unidadeId,
        TipoVinculo tipoVinculo,
        String nomeSolicitado,
        StatusSolicitacaoVinculo status,
        Instant solicitadoEm) {

    public static SolicitacaoVinculo de(br.com.morah.model.SolicitacaoVinculo solicitacao) {
        return new SolicitacaoVinculo(solicitacao.getId(), solicitacao.getUnidade().getId(), solicitacao.getTipoVinculo(),
                solicitacao.getNomeSolicitado(), solicitacao.getStatus(), solicitacao.getSolicitadoEm());
    }
}
