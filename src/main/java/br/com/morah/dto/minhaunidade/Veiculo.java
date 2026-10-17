package br.com.morah.dto.minhaunidade;

public record Veiculo(Long id, Long unidadeId, String placa, String modelo, String cor, TipoVeiculo tipo) {

    public static Veiculo de(br.com.morah.model.minhaunidade.Veiculo veiculo) {
        return new Veiculo(veiculo.getId(), veiculo.getUnidade().getId(), veiculo.getPlaca(), veiculo.getModelo(),
                veiculo.getCor(), veiculo.getTipo());
    }
}
