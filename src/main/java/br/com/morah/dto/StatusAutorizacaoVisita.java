package br.com.morah.dto;

import com.fasterxml.jackson.annotation.JsonValue;

public enum StatusAutorizacaoVisita {
    PENDENTE("pendente"),
    AUTORIZADA("autorizada"),
    RECUSADA("recusada"),
    // nunca é gravado: calculado na leitura (pendente há mais de 30 minutos)
    EXPIRADA("expirada");

    private final String valor;

    StatusAutorizacaoVisita(String valor) {
        this.valor = valor;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }
}
