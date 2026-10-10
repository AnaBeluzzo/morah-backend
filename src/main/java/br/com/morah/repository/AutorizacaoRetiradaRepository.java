package br.com.morah.repository;

import br.com.morah.dto.StatusAutorizacaoRetirada;
import br.com.morah.model.AutorizacaoRetirada;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AutorizacaoRetiradaRepository extends JpaRepository<AutorizacaoRetirada, Long> {

    Optional<AutorizacaoRetirada> findFirstByEncomendaIdAndCodigoAndStatus(Long encomendaId, String codigo,
                                                                          StatusAutorizacaoRetirada status);
}
