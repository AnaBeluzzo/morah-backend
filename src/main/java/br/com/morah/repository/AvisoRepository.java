package br.com.morah.repository;

import br.com.morah.dto.PrioridadeAviso;
import br.com.morah.model.Aviso;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;

public interface AvisoRepository extends JpaRepository<Aviso, Long> {

    Optional<Aviso> findByIdAndCondominioId(Long id, Long condominioId);

    /** Avisos vigentes do condomínio; "todos" = síndico/portaria, senão filtra pelo público-alvo. */
    @Query("""
            select a from Aviso a
            where a.condominio.id = :condominioId
              and (a.expiraEm is null or a.expiraEm > :agora)
              and (:categoria is null or a.categoria = :categoria)
              and (:prioridade is null or a.prioridade = :prioridade)
              and (:todos = true
                   or a.publicoAlvo.tipo = br.com.morah.dto.TipoPublicoAlvo.CONDOMINIO
                   or (a.publicoAlvo.tipo = br.com.morah.dto.TipoPublicoAlvo.BLOCO and a.publicoAlvo.blocoId = :blocoId)
                   or (a.publicoAlvo.tipo = br.com.morah.dto.TipoPublicoAlvo.UNIDADE and a.publicoAlvo.unidadeId = :unidadeId))
            order by a.publicadoEm desc, a.id desc
            """)
    Page<Aviso> buscarVigentes(Long condominioId, Instant agora, String categoria, PrioridadeAviso prioridade,
                               boolean todos, Long blocoId, Long unidadeId, Pageable pageable);
}
