package br.com.morah.repository.minhaunidade;

import br.com.morah.model.minhaunidade.VinculoUnidade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VinculoUnidadeRepository extends JpaRepository<VinculoUnidade, Long> {

    List<VinculoUnidade> findByUnidadeIdOrderById(Long unidadeId);

    Optional<VinculoUnidade> findByIdAndUnidadeBlocoCondominioId(Long id, Long condominioId);
}
