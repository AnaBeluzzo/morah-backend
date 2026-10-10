package br.com.morah.dto;

import com.fasterxml.jackson.annotation.JsonValue;

public enum StatusAutorizacaoRetirada {
    ATIVA("ativa"),
    UTILIZADA("utilizada"),
    // nunca é gravado: calculado na leitura (ativa com validade vencida)
    EXPIRADA("expirada");

    private final String valor;

    StatusAutorizacaoRetirada(String valor) {
        this.valor = valor;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }
}
