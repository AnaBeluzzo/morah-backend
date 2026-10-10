package br.com.morah.repository.portaria;

import br.com.morah.model.portaria.Visitante;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VisitanteRepository extends JpaRepository<Visitante, Long> {

    Optional<Visitante> findByIdAndCondominioId(Long id, Long condominioId);
}
