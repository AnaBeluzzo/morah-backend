package br.com.morah.service;

import br.com.morah.dto.minhaunidade.VeiculoCreateRequest;
import br.com.morah.dto.minhaunidade.VeiculoUpdateRequest;
import br.com.morah.exception.ApiException;
import br.com.morah.model.minhaunidade.Veiculo;
import br.com.morah.repository.condominio.UnidadeRepository;
import br.com.morah.repository.minhaunidade.VeiculoRepository;
import br.com.morah.security.UsuarioLogado;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Veículos da unidade do contexto ativo (tabela database first, ver schema.sql). */
@Service
public class VeiculoService {

    private final VeiculoRepository veiculoRepository;
    private final UnidadeRepository unidadeRepository;

    public VeiculoService(VeiculoRepository veiculoRepository, UnidadeRepository unidadeRepository) {
        this.veiculoRepository = veiculoRepository;
        this.unidadeRepository = unidadeRepository;
    }

    @Transactional(readOnly = true)
    public List<br.com.morah.dto.minhaunidade.Veiculo> buscarVeiculos(UsuarioLogado usuario) {
        return veiculoRepository.findByUnidadeIdOrderById(usuario.unidadeId()).stream()
                .map(br.com.morah.dto.minhaunidade.Veiculo::de)
                .toList();
    }

    @Transactional
    public br.com.morah.dto.minhaunidade.Veiculo criarVeiculo(UsuarioLogado usuario, VeiculoCreateRequest request) {
        Veiculo veiculo = new Veiculo();
        veiculo.setUnidade(unidadeRepository.buscarDoCondominio(usuario.unidadeId(), usuario.condominioId()));
        veiculo.setPlaca(placaDisponivel(request.placa()));
        veiculo.setModelo(request.modelo().trim());
        veiculo.setCor(semVazio(request.cor()));
        veiculo.setTipo(request.tipo());
        return br.com.morah.dto.minhaunidade.Veiculo.de(veiculoRepository.save(veiculo));
    }

    @Transactional
    public br.com.morah.dto.minhaunidade.Veiculo alterarVeiculo(UsuarioLogado usuario, Long veiculoId, VeiculoUpdateRequest request) {
        Veiculo veiculo = buscarPermitido(usuario, veiculoId);
        // PATCH: só altera o que foi enviado
        if (request.placa() != null && !normalizar(request.placa()).equals(veiculo.getPlaca())) {
            veiculo.setPlaca(placaDisponivel(request.placa()));
        }
        if (request.modelo() != null) {
            veiculo.setModelo(request.modelo().trim());
        }
        if (request.cor() != null) {
            veiculo.setCor(semVazio(request.cor()));
        }
        if (request.tipo() != null) {
            veiculo.setTipo(request.tipo());
        }
        return br.com.morah.dto.minhaunidade.Veiculo.de(veiculo);
    }

    @Transactional
    public void deletarVeiculo(UsuarioLogado usuario, Long veiculoId) {
        veiculoRepository.delete(buscarPermitido(usuario, veiculoId));
    }

    /** 404 se for de outro condomínio; 403 se for de outra unidade. */
    private Veiculo buscarPermitido(UsuarioLogado usuario, Long veiculoId) {
        Veiculo veiculo = veiculoRepository.findByIdAndUnidadeBlocoCondominioId(veiculoId, usuario.condominioId())
                .orElseThrow(() -> ApiException.naoEncontrado("Veículo não encontrado"));
        usuario.verificarUnidade(veiculo.getUnidade().getId());
        return veiculo;
    }

    /** A placa é única no banco (UNIQUE): repetida → 409 antes de chegar na constraint. */
    private String placaDisponivel(String placa) {
        String normalizada = normalizar(placa);
        if (veiculoRepository.existsByPlaca(normalizada)) {
            throw ApiException.conflito("Já existe um veículo cadastrado com a placa " + normalizada);
        }
        return normalizada;
    }

    /** Cor em branco vira null: no PATCH, "" apaga a cor. */
    private static String semVazio(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }

    /** "abc-1d23" → "ABC1D23": é assim que a placa fica gravada. */
    private static String normalizar(String placa) {
        return placa.replace("-", "").toUpperCase();
    }
}
