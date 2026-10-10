package br.com.morah.dto;

import java.time.Instant;

public record ModuloCondominio(ModuloCodigo modulo, String nome, boolean ativo, Instant atualizadoEm) {

    public static ModuloCondominio de(br.com.morah.model.ModuloCondominio modulo) {
        return new ModuloCondominio(modulo.getModulo(), modulo.getModulo().getNome(), modulo.isAtivo(),
                modulo.getAtualizadoEm());
    }
}
