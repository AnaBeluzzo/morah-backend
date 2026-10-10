package br.com.morah.repository;

import br.com.morah.dto.StatusCobranca;
import br.com.morah.model.Cobranca;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CobrancaRepository extends JpaRepository<Cobranca, Long> {

    Optional<Cobranca> findByIdAndUnidadeBlocoCondominioId(Long id, Long condominioId);

    boolean existsByIdAndUnidadeBlocoCondominioId(Long id, Long condominioId);

    boolean existsByTaxaIdAndUnidadeId(Long taxaId, Long unidadeId);

    List<Cobranca> findByUnidadeBlocoCondominioIdAndStatus(Long condominioId, StatusCobranca status);

    /** Filtros opcionais (nulo = sem filtro). As datas permitem separar "pendente" de "atrasado". */
    @Query("""
            select c from Cobranca c
            where c.unidade.bloco.condominio.id = :condominioId
              and (:unidadeId is null or c.unidade.id = :unidadeId)
              and (:competencia is null or c.competencia = :competencia)
              and (:status is null or c.status = :status)
              and (:vencidoAntesDe is null or c.vencimento < :vencidoAntesDe)
              and (:venceAPartirDe is null or c.vencimento >= :venceAPartirDe)
            order by c.vencimento desc, c.id desc
            """)
    Page<Cobranca> buscarComFiltros(Long condominioId, Long unidadeId, String competencia, StatusCobranca status,
                                    LocalDate vencidoAntesDe, LocalDate venceAPartirDe, Pageable pageable);
}
