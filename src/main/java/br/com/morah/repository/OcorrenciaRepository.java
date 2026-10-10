package br.com.morah.repository;

import br.com.morah.model.Ocorrencia;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OcorrenciaRepository extends JpaRepository<Ocorrencia, Long> {

    long countByReservaId(Long reservaId);
}
