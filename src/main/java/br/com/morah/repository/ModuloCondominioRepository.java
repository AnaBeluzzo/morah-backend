package br.com.morah.repository;

import br.com.morah.dto.ModuloCodigo;
import br.com.morah.model.ModuloCondominio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ModuloCondominioRepository extends JpaRepository<ModuloCondominio, Long> {

    List<ModuloCondominio> findByCondominioIdOrderById(Long condominioId);

    List<ModuloCondominio> findByCondominioIdAndAtivoFalse(Long condominioId);

    Optional<ModuloCondominio> findByCondominioIdAndModulo(Long condominioId, ModuloCodigo modulo);
}
