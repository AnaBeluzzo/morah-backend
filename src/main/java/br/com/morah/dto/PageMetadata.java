package br.com.morah.dto;

import org.springframework.data.domain.Page;

public record PageMetadata(int page, int size, long totalElements, int totalPages) {

    /** Converte o Page do Spring para o formato do contrato (o Page nunca sai direto na resposta). */
    public static PageMetadata de(Page<?> pagina) {
        return new PageMetadata(pagina.getNumber(), pagina.getSize(), pagina.getTotalElements(), pagina.getTotalPages());
    }
}
