package br.com.morah.repository;

import br.com.morah.dto.StatusEncomenda;
import br.com.morah.model.Encomenda;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;

public interface EncomendaRepository extends JpaRepository<Encomenda, Long> {

    Optional<Encomenda> findByIdAndUnidadeBlocoCondominioId(Long id, Long condominioId);

    long countByUnidadeBlocoCondominioIdAndRecebidaEmGreaterThanEqual(Long condominioId, Instant inicio);

    @Query("""
            select e from Encomenda e
            where e.unidade.bloco.condominio.id = :condominioId
              and (:unidadeId is null or e.unidade.id = :unidadeId)
              and (:status is null or e.status = :status)
            order by e.recebidaEm desc, e.id desc
            """)
    Page<Encomenda> buscarComFiltros(Long condominioId, Long unidadeId, StatusEncomenda status, Pageable pageable);
}
