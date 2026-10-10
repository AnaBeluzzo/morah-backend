package br.com.morah.dto.personalizacao;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

public record Personalizacao(
        Long condominioId,
        String nomeExibicao,
        // exceção à regra non_null: sem logo, o campo aparece como null
        @JsonInclude(JsonInclude.Include.ALWAYS) String logoUrl,
        String corPrimaria,
        String corSecundaria,
        String corDestaque,
        Instant atualizadoEm) {

    public static Personalizacao de(br.com.morah.model.personalizacao.Personalizacao personalizacao) {
        return new Personalizacao(personalizacao.getCondominio().getId(), personalizacao.getNomeExibicao(),
                personalizacao.getLogoUrl(), personalizacao.getCorPrimaria(), personalizacao.getCorSecundaria(),
                personalizacao.getCorDestaque(), personalizacao.getAtualizadoEm());
    }
}
