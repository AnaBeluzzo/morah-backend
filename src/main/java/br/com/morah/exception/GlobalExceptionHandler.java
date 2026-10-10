package br.com.morah.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.Map;

/**
 * Ponto único de tratamento de erros: toda falha vira um ProblemDetail
 * (application/problem+json) com title, status e detail em português.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Também usado pelos handlers de segurança (401/403), para manter o mesmo formato. */
    public static ProblemDetail criarProblema(HttpStatus status, String detalhe) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
        problema.setTitle(switch (status) {
            case BAD_REQUEST -> "Requisição inválida";
            case UNAUTHORIZED -> "Não autenticado";
            case FORBIDDEN -> "Acesso negado";
            case NOT_FOUND -> "Recurso não encontrado";
            case CONFLICT -> "Conflito";
            case GONE -> "Recurso expirado";
            case INTERNAL_SERVER_ERROR -> "Erro interno";
            default -> "Erro na requisição";
        });
        return problema;
    }

    @ExceptionHandler(ApiException.class)
    public ProblemDetail tratarApiException(ApiException e) {
        return criarProblema(e.getStatus(), e.getMessage());
    }

    /** Erros de Bean Validation no corpo (@Valid): devolve a lista "erros". */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail tratarCorpoInvalido(MethodArgumentNotValidException e) {
        List<Map<String, String>> erros = e.getBindingResult().getFieldErrors().stream()
                .map(erro -> erroCampo(erro.getField(), erro.getDefaultMessage()))
                .toList();
        return criarProblemaValidacao(erros);
    }

    /** Erros de Bean Validation em parâmetros (ex.: size=101). */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ProblemDetail tratarParametroInvalido(HandlerMethodValidationException e) {
        List<Map<String, String>> erros = e.getParameterValidationResults().stream()
                .flatMap(resultado -> resultado.getResolvableErrors().stream()
                        .map(erro -> erroCampo(resultado.getMethodParameter().getParameterName(),
                                erro.getDefaultMessage())))
                .toList();
        return criarProblemaValidacao(erros);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail tratarJsonInvalido(HttpMessageNotReadableException e) {
        return criarProblema(HttpStatus.BAD_REQUEST, "Corpo da requisição ausente ou mal formatado");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail tratarTipoInvalido(MethodArgumentTypeMismatchException e) {
        return criarProblemaValidacao(List.of(erroCampo(e.getName(), "valor inválido: " + e.getValue())));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail tratarParametroAusente(MissingServletRequestParameterException e) {
        return criarProblemaValidacao(List.of(erroCampo(e.getParameterName(), "parâmetro obrigatório")));
    }

    /** Negado pelo @PreAuthorize: o perfil ativo não está na lista do contrato. */
    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail tratarAcessoNegado(AccessDeniedException e) {
        return criarProblema(HttpStatus.FORBIDDEN, "O perfil ativo não tem permissão para esta operação");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail tratarErroInesperado(Exception e) {
        // exceções do próprio Spring MVC (rota inexistente, método não suportado...) mantêm o status
        if (e instanceof ErrorResponse erroSpring) {
            HttpStatus status = HttpStatus.valueOf(erroSpring.getStatusCode().value());
            return criarProblema(status, "Requisição não suportada: " + status.getReasonPhrase());
        }
        log.error("Erro inesperado", e);
        return criarProblema(HttpStatus.INTERNAL_SERVER_ERROR, "Erro inesperado no servidor");
    }

    private ProblemDetail criarProblemaValidacao(List<Map<String, String>> erros) {
        ProblemDetail problema = criarProblema(HttpStatus.BAD_REQUEST, "Um ou mais campos são inválidos");
        problema.setProperty("erros", erros);
        return problema;
    }

    private Map<String, String> erroCampo(String campo, String mensagem) {
        return Map.of("campo", campo, "mensagem", mensagem == null ? "valor inválido" : mensagem);
    }
}
