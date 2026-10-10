package br.com.morah.pattern.templatemethod.crud;

import br.com.morah.security.UsuarioLogado;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.function.Function;

/**
 * Template Method.
 * Problema que resolve: todo endpoint de CRUD repetia o mesmo roteiro
 * (obter o usuário logado → validar payload → executar → verificar escopo → responder).
 * O roteiro fica fixo aqui; cada controller só preenche os ganchos validar() e verificarEscopo().
 * O RBAC por perfil fica no @PreAuthorize; aqui é o escopo (403 outra unidade, 404 outro condomínio).
 *
 * O fluxo roda dentro de uma transação: se o escopo for negado depois de executar,
 * tudo o que foi executado é desfeito.
 */
public abstract class BaseCrudController<T> {

    @Autowired
    TransactionTemplate transacao;

    /** Gancho opcional: regras do payload que o Bean Validation não cobre. */
    protected void validar(Object input) {
    }

    /** Gancho obrigatório: lança 403/404 quando o usuário não pode acessar o recurso. */
    protected abstract void verificarEscopo(T recurso, UsuarioLogado usuario);

    protected ResponseEntity<T> criar(Object input, Function<UsuarioLogado, T> acao) {
        T recurso = executarFluxo(input, acao, List::of);
        return ResponseEntity.status(HttpStatus.CREATED).body(recurso);
    }

    protected ResponseEntity<T> alterar(Object input, Function<UsuarioLogado, T> acao) {
        return ResponseEntity.ok(executarFluxo(input, acao, List::of));
    }

    protected ResponseEntity<Void> deletar(Function<UsuarioLogado, T> acao) {
        executarFluxo(null, acao, List::of);
        return ResponseEntity.noContent().build();
    }

    protected ResponseEntity<T> buscar(Function<UsuarioLogado, T> acao) {
        return ResponseEntity.ok(executarFluxo(null, acao, List::of));
    }

    /** Listas: o escopo é verificado em cada item devolvido. */
    protected <P> ResponseEntity<P> buscarTodos(Function<UsuarioLogado, P> acao, Function<P, List<T>> itens) {
        return ResponseEntity.ok(executarFluxo(null, acao, itens));
    }

    /** O método-template: os passos e a ordem nunca mudam. */
    private <R> R executarFluxo(Object input, Function<UsuarioLogado, R> acao, Function<R, List<T>> recursos) {
        return transacao.execute(status -> {
            UsuarioLogado usuario = UsuarioLogado.atual();                         // 1. obter o usuário do token
            if (input != null) {
                validar(input);                                                    // 2. validar o payload
            }
            R resultado = acao.apply(usuario);                                     // 3. executar
            recursos.apply(resultado).forEach(r -> verificarEscopo(r, usuario));   // 4. verificar o escopo
            return resultado;                                                      // 5. responder (quem chamou monta o status)
        });
    }
}
