package br.com.morah.dto.portaria;

import jakarta.validation.constraints.NotNull;

public record DecisaoAutorizacaoRequest(
        @NotNull DecisaoAutorizacao decisao,
        String justificativa) {
}
