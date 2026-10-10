package br.com.morah.dto.minhaunidade;

import br.com.morah.dto.comum.PessoaResumo;

import java.time.LocalDate;

public record VinculoUnidade(
        Long id,
        Long unidadeId,
        PessoaResumo pessoa,
        TipoVinculo tipoVinculo,
        boolean principal,
        LocalDate inicio,
        LocalDate fim) {

    public static VinculoUnidade de(br.com.morah.model.minhaunidade.VinculoUnidade vinculo) {
        return new VinculoUnidade(vinculo.getId(), vinculo.getUnidade().getId(), PessoaResumo.de(vinculo.getPessoa()),
                vinculo.getTipoVinculo(), vinculo.isPrincipal(), vinculo.getInicio(), vinculo.getFim());
    }
}
