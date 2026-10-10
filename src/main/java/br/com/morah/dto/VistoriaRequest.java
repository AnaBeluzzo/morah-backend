package br.com.morah.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record VistoriaRequest(
        @NotNull MomentoVistoria momento,
        String observacao,
        Boolean dano,
        List<String> fotosUrl) {
}
