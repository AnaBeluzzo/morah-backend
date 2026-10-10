package br.com.morah.dto.personalizacao;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ModuloCodigo {
    FINANCEIRO("financeiro", "Financeiro"),
    PORTARIA("portaria", "Portaria"),
    AVISOS("avisos", "Avisos"),
    RESERVAS("reservas", "Reservas"),
    MINHA_UNIDADE("minha_unidade", "Minha Unidade"),
    ENCOMENDAS("encomendas", "Encomendas");

    private final String valor;
    // nome exibido no app, preenchido pelo servidor
    private final String nome;

    ModuloCodigo(String valor, String nome) {
        this.valor = valor;
        this.nome = nome;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }

    public String getNome() {
        return nome;
    }
}
