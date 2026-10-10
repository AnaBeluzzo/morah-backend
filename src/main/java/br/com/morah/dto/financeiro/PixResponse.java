package br.com.morah.dto.financeiro;

import java.time.Instant;

public record PixResponse(String codigoPix, Instant expiraEm) {
}
