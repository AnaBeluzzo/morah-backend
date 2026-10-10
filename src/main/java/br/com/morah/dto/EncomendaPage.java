package br.com.morah.dto;

import java.util.List;

public record EncomendaPage(List<Encomenda> content, PageMetadata page) {
}
