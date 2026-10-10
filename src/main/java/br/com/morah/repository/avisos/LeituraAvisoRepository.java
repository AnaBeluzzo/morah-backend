package br.com.morah.repository.avisos;

import br.com.morah.model.avisos.LeituraAviso;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeituraAvisoRepository extends JpaRepository<LeituraAviso, Long> {

    boolean existsByAvisoIdAndPessoaId(Long avisoId, Long pessoaId);
}
