package br.com.morah.dto.financeiro;

import java.math.BigDecimal;

public record InadimplenciaResumo(
        int totalUnidades,
        int unidadesInadimplentes,
        BigDecimal percentualInadimplencia,
        BigDecimal valorTotalEmAberto) {
}
