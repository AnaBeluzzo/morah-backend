package br.com.morah.repository.encomendas;

import br.com.morah.dto.encomendas.StatusOcorrencia;
import br.com.morah.model.encomendas.Ocorrencia;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OcorrenciaRepository extends JpaRepository<Ocorrencia, Long> {

    long countByReservaId(Long reservaId);

    long countByCondominioIdAndStatus(Long condominioId, StatusOcorrencia status);
}
