package br.com.morah.dto;

import br.com.morah.model.Pessoa;

public record PessoaResumo(Long id, String nome, String telefone) {

    public static PessoaResumo de(Pessoa pessoa) {
        return new PessoaResumo(pessoa.getId(), pessoa.getNome(), pessoa.getTelefone());
    }
}
