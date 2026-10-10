package br.com.morah.dto.avisos;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TipoPublicoAlvo {
    CONDOMINIO("condominio"),
    BLOCO("bloco"),
    UNIDADE("unidade");

    private final String valor;

    TipoPublicoAlvo(String valor) {
        this.valor = valor;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }
}
