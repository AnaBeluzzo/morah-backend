package br.com.morah.dto.comum;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public record PageMetadata(int page, int size, long totalElements, int totalPages) {

    /** Converte o Page do Spring para o formato do contrato (o Page nunca sai direto na resposta). */
    public static PageMetadata de(Page<?> pagina) {
        return new PageMetadata(pagina.getNumber(), pagina.getSize(), pagina.getTotalElements(), pagina.getTotalPages());
    }

    /** Monta a página do contrato: converte cada entidade em DTO e junta os metadados. */
    public static <E, D, P> P paginar(Page<E> pagina, Function<E, D> conversor,
                                      BiFunction<List<D>, PageMetadata, P> construtor) {
        return construtor.apply(pagina.map(conversor).getContent(), de(pagina));
    }
}
