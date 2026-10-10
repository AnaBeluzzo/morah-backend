package br.com.morah.dto;

import com.fasterxml.jackson.annotation.JsonValue;

public enum MomentoVistoria {
    ENTRADA("entrada"),
    SAIDA("saida");

    private final String valor;

    MomentoVistoria(String valor) {
        this.valor = valor;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }
}
