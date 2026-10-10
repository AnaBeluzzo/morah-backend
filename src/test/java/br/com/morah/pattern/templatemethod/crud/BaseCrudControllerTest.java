package br.com.morah.pattern.templatemethod.crud;

import br.com.morah.exception.ApiException;
import br.com.morah.security.UsuarioLogado;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BaseCrudControllerTest {

    private final List<String> passos = new ArrayList<>();

    /** Controller mínimo que só registra a ordem em que os ganchos são chamados. */
    private class ControllerDeTeste extends BaseCrudController<String> {

        @Override
        protected void validar(Object input) {
            passos.add("validar");
        }

        @Override
        protected void verificarEscopo(String recurso, UsuarioLogado usuario) {
            passos.add("verificarEscopo");
            if (recurso.equals("de outra unidade")) {
                throw ApiException.proibido("Recurso de outra unidade");
            }
        }

        ResponseEntity<String> criarTeste(String input) {
            return criar(input, usuario -> {
                passos.add("executar:" + usuario.pessoaId());
                return input;
            });
        }
    }

    /** Gerenciador de transação falso que só anota commit/rollback. */
    private class TransacaoFalsa extends AbstractPlatformTransactionManager {
        @Override
        protected Object doGetTransaction() {
            return new Object();
        }

        @Override
        protected void doBegin(Object transacao, TransactionDefinition definicao) {
        }

        @Override
        protected void doCommit(DefaultTransactionStatus status) {
            passos.add("commit");
        }

        @Override
        protected void doRollback(DefaultTransactionStatus status) {
            passos.add("rollback");
        }
    }

    private ControllerDeTeste controller;

    @BeforeEach
    void prepararUsuarioLogado() {
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "HS256")
                .subject("7").claim("sid", "sessao").claim("perfil", "morador")
                .claim("condominio_id", 1L).claim("unidade_id", 1L).build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
        controller = new ControllerDeTeste();
        controller.transacao = new TransactionTemplate(new TransacaoFalsa());
    }

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveExecutarOsPassosNaOrdemFixa() {
        ResponseEntity<String> resposta = controller.criarTeste("aviso");

        assertThat(passos).containsExactly("validar", "executar:7", "verificarEscopo", "commit");
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resposta.getBody()).isEqualTo("aviso");
    }

    @Test
    void deveDesfazerATransacaoQuandoOEscopoForNegado() {
        assertThatThrownBy(() -> controller.criarTeste("de outra unidade"))
                .isInstanceOf(ApiException.class);

        assertThat(passos).containsExactly("validar", "executar:7", "verificarEscopo", "rollback");
    }
}
