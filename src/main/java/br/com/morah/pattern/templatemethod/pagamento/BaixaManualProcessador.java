package br.com.morah.pattern.templatemethod.pagamento;

import br.com.morah.dto.login.Perfil;
import br.com.morah.exception.ApiException;
import br.com.morah.security.UsuarioLogado;
import org.springframework.stereotype.Component;

/** Origem "baixa manual": só um síndico do mesmo condomínio da cobrança pode quitar. */
@Component
public class BaixaManualProcessador extends ProcessamentoPagamentoTemplate<Long> {

    @Override
    protected Long validarOrigem(Long cobrancaId) {
        UsuarioLogado usuario = UsuarioLogado.atual();
        if (usuario.perfil() != Perfil.SINDICO) {
            throw ApiException.proibido("Somente o síndico pode registrar baixa manual");
        }
        // cobrança de outro condomínio → 404
        if (!cobrancaRepository.existsByIdAndUnidadeBlocoCondominioId(cobrancaId, usuario.condominioId())) {
            throw ApiException.naoEncontrado("Cobrança não encontrada");
        }
        return cobrancaId;
    }
}
