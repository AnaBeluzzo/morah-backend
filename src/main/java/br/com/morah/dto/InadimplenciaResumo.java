package br.com.morah.dto;

import java.math.BigDecimal;

public record InadimplenciaResumo(
        int totalUnidades,
        int unidadesInadimplentes,
        BigDecimal percentualInadimplencia,
        BigDecimal valorTotalEmAberto) {
}
