package br.com.morah.dto;

import br.com.morah.model.Pessoa;

public record PessoaAutenticada(Long id, String nome, String cpf, String email) {

    public static PessoaAutenticada de(Pessoa pessoa) {
        return new PessoaAutenticada(pessoa.getId(), pessoa.getNome(), pessoa.getCpf(), pessoa.getEmail());
    }
}
