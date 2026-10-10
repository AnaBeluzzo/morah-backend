package br.com.morah.dto.avisos;

import br.com.morah.dto.comum.PageMetadata;

import java.util.List;

public record AvisoPage(List<Aviso> content, PageMetadata page) {
}
