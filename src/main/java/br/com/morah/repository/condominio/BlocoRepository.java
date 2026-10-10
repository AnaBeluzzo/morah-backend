package br.com.morah.repository.condominio;

import br.com.morah.model.condominio.Bloco;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlocoRepository extends JpaRepository<Bloco, Long> {

    boolean existsByIdAndCondominioId(Long id, Long condominioId);
}
