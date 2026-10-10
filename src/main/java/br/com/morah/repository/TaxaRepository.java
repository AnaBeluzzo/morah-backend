package br.com.morah.repository;

import br.com.morah.model.Taxa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaxaRepository extends JpaRepository<Taxa, Long> {

    Optional<Taxa> findByIdAndCondominioId(Long id, Long condominioId);
}
