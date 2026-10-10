package br.com.morah.dto;

import br.com.morah.model.Sessao;

public record ContextoAtivo(Perfil perfil, Long condominioId, String condominioNome, Long unidadeId) {

    public static ContextoAtivo de(Sessao sessao) {
        return new ContextoAtivo(
                sessao.getPerfil(),
                sessao.getCondominio().getId(),
                sessao.getCondominio().getNome(),
                sessao.getUnidade() == null ? null : sessao.getUnidade().getId());
    }
}
