package br.com.morah.repository;

import br.com.morah.dto.StatusAcesso;
import br.com.morah.model.Acesso;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;

public interface AcessoRepository extends JpaRepository<Acesso, Long> {

    Optional<Acesso> findFirstByVisitanteIdAndStatusOrderByEntradaDesc(Long visitanteId, StatusAcesso status);

    @Query("""
            select a from Acesso a
            where a.visitante.condominio.id = :condominioId
              and (:inicio is null or a.entrada >= :inicio)
              and (:fim is null or a.entrada < :fim)
            order by a.entrada desc, a.id desc
            """)
    Page<Acesso> buscarDoCondominio(Long condominioId, Instant inicio, Instant fim, Pageable pageable);
}
