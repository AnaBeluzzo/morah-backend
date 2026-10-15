package br.com.morah.repository.condominio;

import br.com.morah.exception.ApiException;
import br.com.morah.model.condominio.Unidade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UnidadeRepository extends JpaRepository<Unidade, Long> {

    Optional<Unidade> findByIdAndBlocoCondominioId(Long id, Long condominioId);

    List<Unidade> findByBlocoCondominioIdOrderById(Long condominioId);

    /** Unidade de outro condomínio (ou inexistente) → 404. */
    default Unidade buscarDoCondominio(Long id, Long condominioId) {
        return findByIdAndBlocoCondominioId(id, condominioId)
                .orElseThrow(() -> ApiException.naoEncontrado("Unidade não encontrada"));
    }
}
