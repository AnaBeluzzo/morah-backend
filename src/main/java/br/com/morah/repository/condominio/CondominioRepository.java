package br.com.morah.repository.condominio;

import br.com.morah.model.condominio.Condominio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CondominioRepository extends JpaRepository<Condominio, Long> {
}
