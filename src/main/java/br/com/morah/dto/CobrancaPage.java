package br.com.morah.dto;

import java.util.List;

public record CobrancaPage(List<Cobranca> content, PageMetadata page) {
}
