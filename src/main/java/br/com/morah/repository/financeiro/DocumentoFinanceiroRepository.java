package br.com.morah.repository.financeiro;

import br.com.morah.model.financeiro.DocumentoFinanceiro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentoFinanceiroRepository extends JpaRepository<DocumentoFinanceiro, Long> {

    Page<DocumentoFinanceiro> findByCondominioIdOrderByPublicadoEmDescIdDesc(Long condominioId, Pageable pageable);
}
