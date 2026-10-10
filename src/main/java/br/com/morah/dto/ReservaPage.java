package br.com.morah.dto;

import java.util.List;

public record ReservaPage(List<Reserva> content, PageMetadata page) {
}
