package br.com.morah.dto.login;

import br.com.morah.model.login.Sessao;

public record ContextoAtivo(Perfil perfil, Long condominioId, String condominioNome, Long unidadeId) {

    public static ContextoAtivo de(Sessao sessao) {
        return new ContextoAtivo(
                sessao.getPerfil(),
                sessao.getCondominio().getId(),
                sessao.getCondominio().getNome(),
                sessao.getUnidade() == null ? null : sessao.getUnidade().getId());
    }
}
