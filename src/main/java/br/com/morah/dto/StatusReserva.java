package br.com.morah.dto;

import com.fasterxml.jackson.annotation.JsonValue;

public enum StatusReserva {
    CONFIRMADA("confirmada"),
    CANCELADA("cancelada"),
    CONCLUIDA("concluida"),
    NAO_COMPARECEU("nao_compareceu");

    private final String valor;

    StatusReserva(String valor) {
        this.valor = valor;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }
}
