package br.com.morah.dto;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TipoRateio {
    IGUALITARIO("igualitario"),
    FRACAO_IDEAL("fracao_ideal");

    private final String valor;

    TipoRateio(String valor) {
        this.valor = valor;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }
}
