package br.com.morah.dto;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TipoAcesso {
    ENTRADA("entrada"),
    SAIDA("saida");

    private final String valor;

    TipoAcesso(String valor) {
        this.valor = valor;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }
}
