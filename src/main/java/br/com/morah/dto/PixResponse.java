package br.com.morah.dto;

import java.time.Instant;

public record PixResponse(String codigoPix, Instant expiraEm) {
}
