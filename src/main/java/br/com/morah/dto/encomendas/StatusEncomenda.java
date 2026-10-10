package br.com.morah.dto.encomendas;

import com.fasterxml.jackson.annotation.JsonValue;

public enum StatusEncomenda {
    AGUARDANDO_RETIRADA("aguardando_retirada"),
    RETIRADA("retirada"),
    EXTRAVIADA("extraviada");

    private final String valor;

    StatusEncomenda(String valor) {
        this.valor = valor;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }
}
