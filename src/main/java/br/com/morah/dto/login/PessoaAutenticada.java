package br.com.morah.dto.login;

import br.com.morah.model.login.Pessoa;

public record PessoaAutenticada(Long id, String nome, String cpf, String email) {

    public static PessoaAutenticada de(Pessoa pessoa) {
        return new PessoaAutenticada(pessoa.getId(), pessoa.getNome(), pessoa.getCpf(), pessoa.getEmail());
    }
}
