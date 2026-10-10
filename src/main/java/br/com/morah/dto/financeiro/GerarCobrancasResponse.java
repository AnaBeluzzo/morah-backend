package br.com.morah.dto.financeiro;

import java.util.List;

public record GerarCobrancasResponse(Long taxaId, int totalGeradas, List<FalhaGeracaoCobranca> falhas) {
}
