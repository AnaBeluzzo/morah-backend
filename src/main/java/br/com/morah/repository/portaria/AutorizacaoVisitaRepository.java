package br.com.morah.repository.portaria;

import br.com.morah.dto.portaria.StatusAutorizacaoVisita;
import br.com.morah.model.portaria.AutorizacaoVisita;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;

public interface AutorizacaoVisitaRepository extends JpaRepository<AutorizacaoVisita, Long> {

    Optional<AutorizacaoVisita> findByIdAndUnidadeBlocoCondominioId(Long id, Long condominioId);

    /** Filtros opcionais (nulo = sem filtro). As datas separam "pendente" de "expirada". */
    @Query("""
            select a from AutorizacaoVisita a
            where a.unidade.bloco.condominio.id = :condominioId
              and (:unidadeId is null or a.unidade.id = :unidadeId)
              and (:status is null or a.status = :status)
              and (:solicitadoAntesDe is null or a.solicitadoEm < :solicitadoAntesDe)
              and (:solicitadoAPartirDe is null or a.solicitadoEm >= :solicitadoAPartirDe)
            order by a.solicitadoEm desc, a.id desc
            """)
    Page<AutorizacaoVisita> buscarComFiltros(Long condominioId, Long unidadeId, StatusAutorizacaoVisita status,
                                             Instant solicitadoAntesDe, Instant solicitadoAPartirDe, Pageable pageable);
}
