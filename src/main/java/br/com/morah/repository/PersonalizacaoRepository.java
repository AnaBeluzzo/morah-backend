package br.com.morah.repository;

import br.com.morah.model.Personalizacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PersonalizacaoRepository extends JpaRepository<Personalizacao, Long> {

    Optional<Personalizacao> findByCondominioId(Long condominioId);
}
