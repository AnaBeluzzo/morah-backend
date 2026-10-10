package br.com.morah.dto.personalizacao;

import java.time.Instant;

public record ModuloCondominio(ModuloCodigo modulo, String nome, boolean ativo, Instant atualizadoEm) {

    public static ModuloCondominio de(br.com.morah.model.personalizacao.ModuloCondominio modulo) {
        return new ModuloCondominio(modulo.getModulo(), modulo.getModulo().getNome(), modulo.isAtivo(),
                modulo.getAtualizadoEm());
    }
}
