package br.com.morah.dto.comum;

import br.com.morah.model.login.Pessoa;

public record PessoaResumo(Long id, String nome, String telefone) {

    public static PessoaResumo de(Pessoa pessoa) {
        return new PessoaResumo(pessoa.getId(), pessoa.getNome(), pessoa.getTelefone());
    }
}
