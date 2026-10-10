package br.com.morah.dto.login;

import java.util.List;

public record MeResponse(
        PessoaAutenticada pessoa,
        List<PerfilDisponivel> perfisDisponiveis,
        ContextoAtivo contextoAtivo) {
}
