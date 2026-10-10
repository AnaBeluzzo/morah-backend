package br.com.morah.dto;

import com.fasterxml.jackson.annotation.JsonValue;

public enum StatusCobranca {
    PENDENTE("pendente"),
    PAGO("pago"),
    // nunca é gravado: calculado na leitura (pendente com vencimento passado)
    ATRASADO("atrasado"),
    CANCELADO("cancelado");

    private final String valor;

    StatusCobranca(String valor) {
        this.valor = valor;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }
}
