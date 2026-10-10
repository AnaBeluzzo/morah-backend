package br.com.morah.dto.portaria;

import com.fasterxml.jackson.annotation.JsonValue;

public enum DecisaoAutorizacao {
    AUTORIZADO("autorizado"),
    RECUSADO("recusado");

    private final String valor;

    DecisaoAutorizacao(String valor) {
        this.valor = valor;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }
}
