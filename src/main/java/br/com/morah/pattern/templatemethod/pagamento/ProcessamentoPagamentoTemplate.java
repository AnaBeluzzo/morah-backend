package br.com.morah.pattern.templatemethod.pagamento;

import br.com.morah.dto.financeiro.StatusCobranca;
import br.com.morah.exception.ApiException;
import br.com.morah.model.financeiro.Cobranca;
import br.com.morah.pattern.templatemethod.notificacao.NotificacaoFinanceiro;
import br.com.morah.repository.financeiro.CobrancaRepository;
import br.com.morah.repository.login.PessoaPerfilRepository;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Template Method.
 * Problema que resolve: a baixa manual (síndico) e o webhook do gateway quitam uma cobrança
 * pelo mesmo roteiro (validar origem → localizar → verificar → marcar como pago → notificar).
 * Só a validação da origem muda; os passos comuns ficam aqui, uma única vez.
 *
 * @param <E> o que chega de cada origem (id da cobrança na baixa manual, requisição bruta no webhook)
 */
public abstract class ProcessamentoPagamentoTemplate<E> {

    @Autowired
    CobrancaRepository cobrancaRepository;

    @Autowired
    PessoaPerfilRepository pessoaPerfilRepository;

    @Autowired
    NotificacaoFinanceiro notificacaoFinanceiro;

    /** O método-template. Devolve null quando a origem pede para não fazer nada (webhook "falhou"). */
    public final Cobranca processar(E entrada) {
        Long cobrancaId = validarOrigem(entrada);            // 1. validar origem (varia)
        if (cobrancaId == null) {
            return null;
        }
        Cobranca cobranca = localizarCobranca(cobrancaId);   // 2. localizar
        verificarSePodeSerPaga(cobranca);                    // 3. verificar
        marcarComoPago(cobranca);                            // 4. marcar como pago
        notificarUnidade(cobranca);                          // 5. notificar a unidade
        return cobranca;
    }

    /** Passo variável: confere quem está pagando e devolve o id da cobrança (ou null para encerrar). */
    protected abstract Long validarOrigem(E entrada);

    protected Cobranca localizarCobranca(Long cobrancaId) {
        return cobrancaRepository.findById(cobrancaId)
                .orElseThrow(() -> ApiException.naoEncontrado("Cobrança não encontrada"));
    }

    protected void verificarSePodeSerPaga(Cobranca cobranca) {
        if (cobranca.getStatus() == StatusCobranca.PAGO || cobranca.getStatus() == StatusCobranca.CANCELADO) {
            throw ApiException.conflito("Cobrança já está paga ou cancelada");
        }
    }

    protected void marcarComoPago(Cobranca cobranca) {
        cobranca.setStatus(StatusCobranca.PAGO);
    }

    protected void notificarUnidade(Cobranca cobranca) {
        notificacaoFinanceiro.notificar(cobranca, pessoaPerfilRepository.findMoradoresByUnidadeId(cobranca.getUnidade().getId()));
    }
}
