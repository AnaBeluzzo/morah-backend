package br.com.morah.controller;

import br.com.morah.dto.minhaunidade.Veiculo;
import br.com.morah.dto.minhaunidade.VeiculoCreateRequest;
import br.com.morah.dto.minhaunidade.VeiculoUpdateRequest;
import br.com.morah.security.UsuarioLogado;
import br.com.morah.service.VeiculoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Veículos da unidade ativa: qualquer morador ou proprietário da unidade gerencia. */
@RestController
@RequestMapping("/unidades/minha/veiculos")
@PreAuthorize("hasAnyRole('MORADOR', 'PROPRIETARIO')")
public class VeiculoController {

    private final VeiculoService veiculoService;

    public VeiculoController(VeiculoService veiculoService) {
        this.veiculoService = veiculoService;
    }

    @GetMapping
    public List<Veiculo> buscarVeiculos() {
        return veiculoService.buscarVeiculos(UsuarioLogado.atual());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Veiculo criarVeiculo(@Valid @RequestBody VeiculoCreateRequest request) {
        return veiculoService.criarVeiculo(UsuarioLogado.atual(), request);
    }

    @PatchMapping("/{veiculoId}")
    public Veiculo alterarVeiculo(@PathVariable Long veiculoId, @Valid @RequestBody VeiculoUpdateRequest request) {
        return veiculoService.alterarVeiculo(UsuarioLogado.atual(), veiculoId, request);
    }

    @DeleteMapping("/{veiculoId}")
    public ResponseEntity<Void> deletarVeiculo(@PathVariable Long veiculoId) {
        veiculoService.deletarVeiculo(UsuarioLogado.atual(), veiculoId);
        return ResponseEntity.noContent().build();
    }
}
