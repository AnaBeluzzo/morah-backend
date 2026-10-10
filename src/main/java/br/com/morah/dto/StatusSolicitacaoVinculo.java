package br.com.morah.dto;

import com.fasterxml.jackson.annotation.JsonValue;

public enum StatusSolicitacaoVinculo {
    PENDENTE("pendente"),
    APROVADA("aprovada"),
    REJEITADA("rejeitada");

    private final String valor;

    StatusSolicitacaoVinculo(String valor) {
        this.valor = valor;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }
}
