package br.com.morah.dto.encomendas;

import br.com.morah.dto.comum.PageMetadata;

import java.util.List;

public record EncomendaPage(List<Encomenda> content, PageMetadata page) {
}
