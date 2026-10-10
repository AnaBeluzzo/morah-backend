package br.com.morah.controller;

import br.com.morah.dto.personalizacao.ModuloCodigo;
import br.com.morah.dto.personalizacao.ModuloCondominio;
import br.com.morah.dto.personalizacao.ModuloUpdateRequest;
import br.com.morah.dto.personalizacao.Personalizacao;
import br.com.morah.dto.personalizacao.PersonalizacaoUpdateRequest;
import br.com.morah.exception.ApiException;
import br.com.morah.security.UsuarioLogado;
import br.com.morah.service.CondominioService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/condominio")
public class CondominioController {

    private final CondominioService condominioService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    public CondominioController(CondominioService condominioService, ObjectMapper objectMapper, Validator validator) {
        this.condominioService = condominioService;
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    @GetMapping("/personalizacao")
    @PreAuthorize("hasAnyRole('MORADOR', 'PROPRIETARIO', 'SINDICO', 'PORTARIA')")
    public Personalizacao buscarPersonalizacao() {
        return condominioService.buscarPersonalizacao(UsuarioLogado.atual());
    }

    /**
     * Único endpoint que recebe o corpo como JsonNode: é a forma de saber se "logoUrl"
     * veio como null (remover a logo) ou simplesmente não veio (manter).
     */
    @PatchMapping("/personalizacao")
    @PreAuthorize("hasRole('SINDICO')")
    public Personalizacao alterarPersonalizacao(@RequestBody JsonNode corpo) {
        PersonalizacaoUpdateRequest request;
        try {
            request = objectMapper.treeToValue(corpo, PersonalizacaoUpdateRequest.class);
        } catch (JsonProcessingException e) {
            throw ApiException.requisicaoInvalida("Corpo da requisição mal formatado");
        }
        Set<ConstraintViolation<PersonalizacaoUpdateRequest>> erros = validator.validate(request);
        if (!erros.isEmpty()) {
            throw new ConstraintViolationException(erros);
        }
        return condominioService.alterarPersonalizacao(UsuarioLogado.atual(), request, corpo.has("logoUrl"));
    }

    @GetMapping("/modulos")
    @PreAuthorize("hasAnyRole('MORADOR', 'PROPRIETARIO', 'SINDICO', 'PORTARIA')")
    public List<ModuloCondominio> buscarModulos() {
        return condominioService.buscarModulos(UsuarioLogado.atual());
    }

    @PatchMapping("/modulos/{modulo}")
    @PreAuthorize("hasRole('SINDICO')")
    public ModuloCondominio alterarModulo(@PathVariable ModuloCodigo modulo, @Valid @RequestBody ModuloUpdateRequest request) {
        return condominioService.alterarModulo(UsuarioLogado.atual(), modulo, request.ativo());
    }
}
