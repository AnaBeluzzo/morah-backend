package br.com.morah.repository.encomendas;

import br.com.morah.dto.encomendas.StatusAutorizacaoRetirada;
import br.com.morah.model.encomendas.AutorizacaoRetirada;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AutorizacaoRetiradaRepository extends JpaRepository<AutorizacaoRetirada, Long> {

    Optional<AutorizacaoRetirada> findFirstByEncomendaIdAndCodigoAndStatus(Long encomendaId, String codigo,
                                                                          StatusAutorizacaoRetirada status);
}
