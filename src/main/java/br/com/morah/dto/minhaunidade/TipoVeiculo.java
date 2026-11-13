package br.com.morah.dto.minhaunidade;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TipoVeiculo {
    CARRO("carro"),
    MOTO("moto");

    private final String valor;

    TipoVeiculo(String valor) {
        this.valor = valor;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }
}
