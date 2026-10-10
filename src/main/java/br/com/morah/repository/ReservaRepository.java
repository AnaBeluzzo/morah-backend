package br.com.morah.repository;

import br.com.morah.dto.StatusReserva;
import br.com.morah.model.Reserva;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    Optional<Reserva> findByIdAndAreaComumCondominioId(Long id, Long condominioId);

    List<Reserva> findByUnidadeIdAndStatusAndInicioGreaterThanEqualOrderByInicioAsc(Long unidadeId, StatusReserva status,
                                                                                    Instant inicio, Pageable pageable);

    /** Sobreposição: começa antes do fim pedido e termina depois do início pedido. */
    boolean existsByAreaComumIdAndStatusAndInicioLessThanAndFimGreaterThan(Long areaComumId, StatusReserva status,
                                                                           Instant fim, Instant inicio);

    List<Reserva> findByAreaComumIdAndStatusAndInicioLessThanAndFimGreaterThan(Long areaComumId, StatusReserva status,
                                                                               Instant fim, Instant inicio);

    @Query("""
            select r from Reserva r
            where r.areaComum.condominio.id = :condominioId
              and (:unidadeId is null or r.unidade.id = :unidadeId)
              and (:inicio is null or r.inicio >= :inicio)
              and (:fim is null or r.inicio < :fim)
            order by r.inicio, r.id
            """)
    Page<Reserva> buscarComFiltros(Long condominioId, Long unidadeId, Instant inicio, Instant fim, Pageable pageable);
}
