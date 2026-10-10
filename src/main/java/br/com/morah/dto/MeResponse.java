package br.com.morah.dto;

import java.util.List;

public record MeResponse(
        PessoaAutenticada pessoa,
        List<PerfilDisponivel> perfisDisponiveis,
        ContextoAtivo contextoAtivo) {
}
