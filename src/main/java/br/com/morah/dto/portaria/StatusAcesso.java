package br.com.morah.dto.portaria;

import com.fasterxml.jackson.annotation.JsonValue;

public enum StatusAcesso {
    NO_CONDOMINIO("no_condominio"),
    SAIU("saiu");

    private final String valor;

    StatusAcesso(String valor) {
        this.valor = valor;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }
}
