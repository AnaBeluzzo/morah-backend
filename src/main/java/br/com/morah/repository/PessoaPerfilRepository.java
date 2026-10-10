package br.com.morah.repository;

import br.com.morah.dto.Perfil;
import br.com.morah.model.PessoaPerfil;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PessoaPerfilRepository extends JpaRepository<PessoaPerfil, Long> {

    List<PessoaPerfil> findByPessoaIdOrderById(Long pessoaId);

    Optional<PessoaPerfil> findFirstByPessoaIdAndCondominioIdAndPerfilOrderById(Long pessoaId, Long condominioId, Perfil perfil);
}
