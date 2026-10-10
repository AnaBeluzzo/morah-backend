package br.com.morah.pattern;

import br.com.morah.dto.TipoNotificacao;
import br.com.morah.model.Notificacao;
import br.com.morah.model.Pessoa;
import br.com.morah.repository.NotificacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.List;

/**
 * Template Method.
 * Problema que resolve: todos os módulos notificam do mesmo jeito
 * (montar payload → escolher canal → enviar → registrar no banco).
 * O roteiro fica fixo aqui; cada módulo só diz o texto da notificação em montarPayload().
 *
 * @param <E> o objeto que gerou a notificação (aviso, encomenda, cobrança...)
 */
public abstract class NotificacaoTemplate<E> {

    @Autowired
    NotificacaoRepository notificacaoRepository;

    @Autowired
    CanalPush canalPush;

    @Autowired
    CanalEmail canalEmail;

    /** O método-template: final para que nenhum módulo mude a ordem dos passos. */
    public final void notificar(E evento, List<Pessoa> destinatarios) {
        Notificacao payload = montarPayload(evento);          // 1. montar payload
        CanalNotificacao canal = escolherCanal(payload);      // 2. escolher canal (Strategy)
        for (Pessoa destinatario : destinatarios) {
            canal.enviar(destinatario, payload);              // 3. enviar
            registrar(destinatario, payload);                 // 4. registrar no banco
        }
    }

    /** Passo variável: cada módulo monta tipo, título, mensagem e referência. */
    protected abstract Notificacao montarPayload(E evento);

    /** Financeiro sai por e-mail; os demais por push. Pode ser sobrescrito (ex.: convite de inquilino). */
    protected CanalNotificacao escolherCanal(Notificacao payload) {
        return payload.getTipo() == TipoNotificacao.FINANCEIRO ? canalEmail : canalPush;
    }

    protected void registrar(Pessoa destinatario, Notificacao payload) {
        Notificacao notificacao = new Notificacao();
        notificacao.setPessoa(destinatario);
        notificacao.setTipo(payload.getTipo());
        notificacao.setTitulo(payload.getTitulo());
        notificacao.setMensagem(payload.getMensagem());
        notificacao.setReferenciaId(payload.getReferenciaId());
        notificacao.setEnviadaEm(Instant.now());
        notificacaoRepository.save(notificacao);
    }

    /** Atalho para as subclasses montarem o payload em uma linha. */
    protected Notificacao payload(TipoNotificacao tipo, String titulo, String mensagem, Long referenciaId) {
        Notificacao payload = new Notificacao();
        payload.setTipo(tipo);
        payload.setTitulo(titulo);
        payload.setMensagem(mensagem);
        payload.setReferenciaId(referenciaId);
        return payload;
    }
}
