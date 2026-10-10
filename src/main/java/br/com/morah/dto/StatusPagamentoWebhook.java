package br.com.morah.dto;

import com.fasterxml.jackson.annotation.JsonValue;

public enum StatusPagamentoWebhook {
    CONFIRMADO("confirmado"),
    FALHOU("falhou");

    private final String valor;

    StatusPagamentoWebhook(String valor) {
        this.valor = valor;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }
}
