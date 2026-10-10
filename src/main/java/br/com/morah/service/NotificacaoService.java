package br.com.morah.service;

import br.com.morah.dto.Notificacao;
import br.com.morah.exception.ApiException;
import br.com.morah.security.UsuarioLogado;
import br.com.morah.repository.NotificacaoRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class NotificacaoService {

    private final NotificacaoRepository notificacaoRepository;

    public NotificacaoService(NotificacaoRepository notificacaoRepository) {
        this.notificacaoRepository = notificacaoRepository;
    }

    /** O contrato pede só o array da página, sem metadados. */
    @Transactional(readOnly = true)
    public List<Notificacao> buscarNotificacoes(UsuarioLogado usuario, int page, int size) {
        return notificacaoRepository.findByPessoaIdOrderByEnviadaEmDescIdDesc(usuario.pessoaId(), PageRequest.of(page, size))
                .stream().map(Notificacao::de).toList();
    }

    @Transactional
    public Notificacao marcarNotificacaoLida(UsuarioLogado usuario, Long notificacaoId) {
        // notificação de outra pessoa é tratada como inexistente (404)
        br.com.morah.model.Notificacao notificacao = notificacaoRepository.findByIdAndPessoaId(notificacaoId, usuario.pessoaId())
                .orElseThrow(() -> ApiException.naoEncontrado("Notificação não encontrada"));
        if (notificacao.getLidaEm() == null) {
            notificacao.setLidaEm(Instant.now());
        }
        return Notificacao.de(notificacao);
    }
}
