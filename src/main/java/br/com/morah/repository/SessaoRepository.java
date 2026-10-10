package br.com.morah.repository;

import br.com.morah.model.Sessao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SessaoRepository extends JpaRepository<Sessao, String> {

    Optional<Sessao> findByRefreshToken(String refreshToken);

    void deleteByPessoaId(Long pessoaId);
}
