package br.com.morah.dto.login;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Perfil {
    MORADOR("morador"),
    PROPRIETARIO("proprietario"),
    SINDICO("sindico"),
    PORTARIA("portaria");

    private final String valor;

    Perfil(String valor) {
        this.valor = valor;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }

    /** Converte o valor da claim "perfil" do JWT de volta para o enum. */
    public static Perfil de(String valor) {
        for (Perfil perfil : values()) {
            if (perfil.valor.equals(valor)) {
                return perfil;
            }
        }
        throw new IllegalArgumentException("Perfil desconhecido: " + valor);
    }
}
