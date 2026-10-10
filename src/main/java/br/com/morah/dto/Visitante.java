package br.com.morah.dto;

public record Visitante(Long id, String nome, String documento, String telefone, String observacao) {

    public static Visitante de(br.com.morah.model.Visitante visitante) {
        return new Visitante(visitante.getId(), visitante.getNome(), visitante.getDocumento(), visitante.getTelefone(),
                visitante.getObservacao());
    }
}
