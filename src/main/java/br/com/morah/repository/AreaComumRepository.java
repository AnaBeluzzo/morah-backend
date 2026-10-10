package br.com.morah.repository;

import br.com.morah.model.AreaComum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AreaComumRepository extends JpaRepository<AreaComum, Long> {

    List<AreaComum> findByCondominioIdOrderById(Long condominioId);

    Optional<AreaComum> findByIdAndCondominioId(Long id, Long condominioId);
}
