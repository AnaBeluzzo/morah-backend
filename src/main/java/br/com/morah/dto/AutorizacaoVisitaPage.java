package br.com.morah.dto;

import java.util.List;

public record AutorizacaoVisitaPage(List<AutorizacaoVisita> content, PageMetadata page) {
}
