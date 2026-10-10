package br.com.morah.dto.login;

import br.com.morah.model.login.PessoaPerfil;

public record PerfilDisponivel(Perfil perfil, Long condominioId, String condominioNome, Long unidadeId) {

    public static PerfilDisponivel de(PessoaPerfil vinculo) {
        return new PerfilDisponivel(
                vinculo.getPerfil(),
                vinculo.getCondominio().getId(),
                vinculo.getCondominio().getNome(),
                vinculo.getUnidade() == null ? null : vinculo.getUnidade().getId());
    }
}
