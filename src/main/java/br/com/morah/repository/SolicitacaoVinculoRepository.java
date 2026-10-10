package br.com.morah.repository;

import br.com.morah.dto.StatusSolicitacaoVinculo;
import br.com.morah.model.SolicitacaoVinculo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SolicitacaoVinculoRepository extends JpaRepository<SolicitacaoVinculo, Long> {

    List<SolicitacaoVinculo> findByUnidadeIdAndStatusOrderById(Long unidadeId, StatusSolicitacaoVinculo status);

    Optional<SolicitacaoVinculo> findByIdAndUnidadeId(Long id, Long unidadeId);
}
