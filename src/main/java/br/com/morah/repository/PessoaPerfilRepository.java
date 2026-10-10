package br.com.morah.repository;

import br.com.morah.dto.Perfil;
import br.com.morah.model.Pessoa;
import br.com.morah.model.PessoaPerfil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PessoaPerfilRepository extends JpaRepository<PessoaPerfil, Long> {

    List<PessoaPerfil> findByPessoaIdOrderById(Long pessoaId);

    Optional<PessoaPerfil> findFirstByPessoaIdAndCondominioIdAndPerfilOrderById(Long pessoaId, Long condominioId, Perfil perfil);

    List<PessoaPerfil> findByCondominioId(Long condominioId);

    List<PessoaPerfil> findByUnidadeBlocoId(Long blocoId);

    List<PessoaPerfil> findByUnidadeId(Long unidadeId);

    /** Moradores (morador ou proprietário) de uma unidade, sem repetição: destinatários das notificações. */
    @Query("select distinct pp.pessoa from PessoaPerfil pp where pp.unidade.id = :unidadeId")
    List<Pessoa> findMoradoresByUnidadeId(Long unidadeId);
}
