package br.com.morah.repository;

import br.com.morah.model.LeituraAviso;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeituraAvisoRepository extends JpaRepository<LeituraAviso, Long> {

    boolean existsByAvisoIdAndPessoaId(Long avisoId, Long pessoaId);
}
