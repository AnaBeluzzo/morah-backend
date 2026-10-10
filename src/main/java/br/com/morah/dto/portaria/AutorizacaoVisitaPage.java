package br.com.morah.dto.portaria;

import br.com.morah.dto.comum.PageMetadata;

import java.util.List;

public record AutorizacaoVisitaPage(List<AutorizacaoVisita> content, PageMetadata page) {
}
