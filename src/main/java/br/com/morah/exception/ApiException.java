package br.com.morah.exception;

import org.springframework.http.HttpStatus;

/**
 * Erro de negócio que já sabe qual status HTTP deve ser devolvido.
 * O GlobalExceptionHandler transforma em ProblemDetail (RFC 9457).
 */
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String detalhe) {
        super(detalhe);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static ApiException requisicaoInvalida(String detalhe) {
        return new ApiException(HttpStatus.BAD_REQUEST, detalhe);
    }

    public static ApiException naoAutenticado(String detalhe) {
        return new ApiException(HttpStatus.UNAUTHORIZED, detalhe);
    }

    public static ApiException proibido(String detalhe) {
        return new ApiException(HttpStatus.FORBIDDEN, detalhe);
    }

    public static ApiException naoEncontrado(String detalhe) {
        return new ApiException(HttpStatus.NOT_FOUND, detalhe);
    }

    public static ApiException conflito(String detalhe) {
        return new ApiException(HttpStatus.CONFLICT, detalhe);
    }

    public static ApiException expirado(String detalhe) {
        return new ApiException(HttpStatus.GONE, detalhe);
    }
}
