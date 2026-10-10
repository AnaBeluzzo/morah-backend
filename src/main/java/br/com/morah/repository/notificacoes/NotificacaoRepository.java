package br.com.morah.repository.notificacoes;

import br.com.morah.model.notificacoes.Notificacao;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {

    List<Notificacao> findByPessoaIdOrderByEnviadaEmDescIdDesc(Long pessoaId, Pageable pageable);

    Optional<Notificacao> findByIdAndPessoaId(Long id, Long pessoaId);
}
