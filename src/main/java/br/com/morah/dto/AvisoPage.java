package br.com.morah.dto;

import java.util.List;

public record AvisoPage(List<Aviso> content, PageMetadata page) {
}
