package br.com.morah.dto.avisos;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PrioridadeAviso {
    NORMAL("normal"),
    URGENTE("urgente");

    private final String valor;

    PrioridadeAviso(String valor) {
        this.valor = valor;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }
}
