package br.com.morah.repository;

import br.com.morah.model.Visitante;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VisitanteRepository extends JpaRepository<Visitante, Long> {

    Optional<Visitante> findByIdAndCondominioId(Long id, Long condominioId);
}
