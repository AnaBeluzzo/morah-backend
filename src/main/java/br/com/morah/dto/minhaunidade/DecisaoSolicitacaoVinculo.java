package br.com.morah.dto.minhaunidade;

import com.fasterxml.jackson.annotation.JsonValue;

public enum DecisaoSolicitacaoVinculo {
    APROVADA("aprovada"),
    REJEITADA("rejeitada");

    private final String valor;

    DecisaoSolicitacaoVinculo(String valor) {
        this.valor = valor;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }
}
