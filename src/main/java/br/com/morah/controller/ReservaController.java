package br.com.morah.controller;

import br.com.morah.dto.reservas.AreaComum;
import br.com.morah.dto.reservas.Disponibilidade;
import br.com.morah.dto.reservas.Reserva;
import br.com.morah.dto.reservas.ReservaCreateRequest;
import br.com.morah.dto.reservas.ReservaPage;
import br.com.morah.dto.reservas.VistoriaRequest;
import br.com.morah.exception.ApiException;
import br.com.morah.pattern.templatemethod.crud.BaseCrudController;
import br.com.morah.security.UsuarioLogado;
import br.com.morah.service.ReservaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/** Áreas comuns e reservas. O CRUD de reservas usa o fluxo fixo do BaseCrudController (Template Method). */
@RestController
public class ReservaController extends BaseCrudController<Reserva> {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @GetMapping("/areas-comuns")
    @PreAuthorize("hasAnyRole('MORADOR', 'PROPRIETARIO', 'SINDICO', 'PORTARIA')")
    public List<AreaComum> buscarAreasComuns() {
        return reservaService.buscarAreasComuns(UsuarioLogado.atual());
    }

    @GetMapping("/areas-comuns/{areaComumId}/disponibilidade")
    @PreAuthorize("hasAnyRole('MORADOR', 'PROPRIETARIO')")
    public Disponibilidade buscarDisponibilidade(@PathVariable Long areaComumId, @RequestParam LocalDate data) {
        return reservaService.buscarDisponibilidade(UsuarioLogado.atual(), areaComumId, data);
    }

    @GetMapping("/reservas")
    @PreAuthorize("hasAnyRole('MORADOR', 'PROPRIETARIO', 'PORTARIA', 'SINDICO')")
    public ResponseEntity<ReservaPage> buscarReservas(@RequestParam(required = false) LocalDate data,
                                                      @RequestParam(defaultValue = "0") @Min(0) int page,
                                                      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return buscarTodos(usuario -> reservaService.buscarReservas(usuario, data, page, size), ReservaPage::content);
    }

    @PostMapping("/reservas")
    @PreAuthorize("hasAnyRole('MORADOR', 'PROPRIETARIO')")
    public ResponseEntity<Reserva> criarReserva(@Valid @RequestBody ReservaCreateRequest request) {
        return criar(request, usuario -> reservaService.criarReserva(usuario, request));
    }

    @GetMapping("/reservas/{reservaId}")
    @PreAuthorize("hasAnyRole('MORADOR', 'PROPRIETARIO', 'PORTARIA', 'SINDICO')")
    public ResponseEntity<Reserva> buscarReserva(@PathVariable Long reservaId) {
        return buscar(usuario -> reservaService.buscarReserva(usuario, reservaId));
    }

    @DeleteMapping("/reservas/{reservaId}")
    @PreAuthorize("hasAnyRole('MORADOR', 'PROPRIETARIO')")
    public ResponseEntity<Void> deletarReserva(@PathVariable Long reservaId) {
        return deletar(usuario -> reservaService.deletarReserva(usuario, reservaId));
    }

    @PostMapping("/reservas/{reservaId}/vistoria")
    @PreAuthorize("hasRole('PORTARIA')")
    public Reserva registrarVistoria(@PathVariable Long reservaId, @Valid @RequestBody VistoriaRequest request) {
        return reservaService.registrarVistoria(UsuarioLogado.atual(), reservaId, request);
    }

    /** Reserva em horas cheias, no mesmo dia, entre 08:00 e 22:00. */
    @Override
    protected void validar(Object input) {
        if (input instanceof ReservaCreateRequest request) {
            LocalDateTime inicio = LocalDateTime.ofInstant(request.inicio(), ZoneId.systemDefault());
            LocalDateTime fim = LocalDateTime.ofInstant(request.fim(), ZoneId.systemDefault());
            boolean horaCheia = inicio.getMinute() == 0 && inicio.getSecond() == 0 && fim.getMinute() == 0 && fim.getSecond() == 0;
            boolean mesmoDia = inicio.toLocalDate().equals(fim.toLocalDate());
            if (!fim.isAfter(inicio) || !horaCheia || !mesmoDia
                    || inicio.getHour() < ReservaService.HORA_ABERTURA || fim.getHour() > ReservaService.HORA_FECHAMENTO) {
                throw ApiException.requisicaoInvalida("A reserva deve ser em horas cheias, no mesmo dia, entre "
                        + ReservaService.HORA_ABERTURA + ":00 e " + ReservaService.HORA_FECHAMENTO + ":00");
            }
        }
    }

    /** Morador/proprietário só acessa reservas da própria unidade (403). */
    @Override
    protected void verificarEscopo(Reserva reserva, UsuarioLogado usuario) {
        usuario.verificarUnidade(reserva.unidadeId());
    }
}
