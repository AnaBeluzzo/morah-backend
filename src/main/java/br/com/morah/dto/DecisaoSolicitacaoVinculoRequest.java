package br.com.morah.dto;

import jakarta.validation.constraints.NotNull;

public record DecisaoSolicitacaoVinculoRequest(
        @NotNull DecisaoSolicitacaoVinculo decisao,
        String justificativa) {
}
