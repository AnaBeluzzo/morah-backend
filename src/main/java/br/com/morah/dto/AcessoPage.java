package br.com.morah.dto;

import java.util.List;

public record AcessoPage(List<Acesso> content, PageMetadata page) {
}
