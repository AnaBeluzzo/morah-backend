package br.com.morah.dto.minhaunidade;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TipoVinculo {
    PROPRIETARIO("proprietario"),
    INQUILINO("inquilino"),
    DEPENDENTE("dependente");

    private final String valor;

    TipoVinculo(String valor) {
        this.valor = valor;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }
}
