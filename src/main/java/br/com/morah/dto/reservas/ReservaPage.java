package br.com.morah.dto.reservas;

import br.com.morah.dto.comum.PageMetadata;

import java.util.List;

public record ReservaPage(List<Reserva> content, PageMetadata page) {
}
