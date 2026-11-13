package br.com.morah.repository.minhaunidade;

import br.com.morah.model.minhaunidade.Veiculo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VeiculoRepository extends JpaRepository<Veiculo, Long> {

    List<Veiculo> findByUnidadeIdOrderById(Long unidadeId);

    Optional<Veiculo> findByIdAndUnidadeBlocoCondominioId(Long id, Long condominioId);

    boolean existsByPlaca(String placa);
}
