package br.com.morah.repository.login;

import br.com.morah.model.login.Pessoa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PessoaRepository extends JpaRepository<Pessoa, Long> {

    Optional<Pessoa> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}
