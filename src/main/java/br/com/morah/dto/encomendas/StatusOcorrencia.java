package br.com.morah.dto.encomendas;

import com.fasterxml.jackson.annotation.JsonValue;

public enum StatusOcorrencia {
    ABERTA("aberta"),
    EM_ANDAMENTO("em_andamento"),
    ENCERRADA("encerrada");

    private final String valor;

    StatusOcorrencia(String valor) {
        this.valor = valor;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }
}
