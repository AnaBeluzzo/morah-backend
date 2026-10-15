package br.com.morah.service;

import br.com.morah.dto.comum.PageMetadata;
import br.com.morah.dto.login.Perfil;
import br.com.morah.dto.reservas.Disponibilidade;
import br.com.morah.dto.reservas.HorarioDisponivel;
import br.com.morah.dto.reservas.MomentoVistoria;
import br.com.morah.dto.reservas.ReservaCreateRequest;
import br.com.morah.dto.reservas.ReservaPage;
import br.com.morah.dto.reservas.StatusReserva;
import br.com.morah.dto.reservas.VistoriaRequest;
import br.com.morah.exception.ApiException;
import br.com.morah.model.encomendas.Ocorrencia;
import br.com.morah.model.reservas.AreaComum;
import br.com.morah.model.reservas.Reserva;
import br.com.morah.model.reservas.Vistoria;
import br.com.morah.pattern.templatemethod.notificacao.NotificacaoReserva;
import br.com.morah.repository.condominio.UnidadeRepository;
import br.com.morah.repository.encomendas.OcorrenciaRepository;
import br.com.morah.repository.login.PessoaRepository;
import br.com.morah.repository.reservas.AreaComumRepository;
import br.com.morah.repository.reservas.ReservaRepository;
import br.com.morah.repository.reservas.VistoriaRepository;
import br.com.morah.security.UsuarioLogado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReservaService {

    // horários de 1 hora, das 08:00 às 22:00 (fuso do servidor)
    public static final int HORA_ABERTURA = 8;
    public static final int HORA_FECHAMENTO = 22;

    private final AreaComumRepository areaComumRepository;
    private final ReservaRepository reservaRepository;
    private final VistoriaRepository vistoriaRepository;
    private final OcorrenciaRepository ocorrenciaRepository;
    private final UnidadeRepository unidadeRepository;
    private final PessoaRepository pessoaRepository;
    private final NotificacaoReserva notificacaoReserva;

    public ReservaService(AreaComumRepository areaComumRepository, ReservaRepository reservaRepository,
                          VistoriaRepository vistoriaRepository, OcorrenciaRepository ocorrenciaRepository,
                          UnidadeRepository unidadeRepository, PessoaRepository pessoaRepository,
                          NotificacaoReserva notificacaoReserva) {
        this.areaComumRepository = areaComumRepository;
        this.reservaRepository = reservaRepository;
        this.vistoriaRepository = vistoriaRepository;
        this.ocorrenciaRepository = ocorrenciaRepository;
        this.unidadeRepository = unidadeRepository;
        this.pessoaRepository = pessoaRepository;
        this.notificacaoReserva = notificacaoReserva;
    }

    @Transactional(readOnly = true)
    public List<br.com.morah.dto.reservas.AreaComum> buscarAreasComuns(UsuarioLogado usuario) {
        return areaComumRepository.findByCondominioIdOrderById(usuario.condominioId()).stream()
                .map(br.com.morah.dto.reservas.AreaComum::de)
                .toList();
    }

    /** Um horário fica indisponível quando há reserva confirmada sobreposta. */
    @Transactional(readOnly = true)
    public Disponibilidade buscarDisponibilidade(UsuarioLogado usuario, Long areaComumId, LocalDate data) {
        AreaComum area = buscarArea(usuario, areaComumId);
        Instant aberturaDoDia = horario(data, HORA_ABERTURA);
        Instant fechamentoDoDia = horario(data, HORA_FECHAMENTO);
        List<Reserva> confirmadas = reservaRepository.findByAreaComumIdAndStatusAndInicioLessThanAndFimGreaterThan(
                area.getId(), StatusReserva.CONFIRMADA, fechamentoDoDia, aberturaDoDia);

        List<HorarioDisponivel> horarios = new ArrayList<>();
        for (int hora = HORA_ABERTURA; hora < HORA_FECHAMENTO; hora++) {
            Instant inicio = horario(data, hora);
            Instant fim = horario(data, hora + 1);
            boolean ocupado = confirmadas.stream()
                    .anyMatch(r -> r.getInicio().isBefore(fim) && r.getFim().isAfter(inicio));
            horarios.add(new HorarioDisponivel(inicio, fim, !ocupado));
        }
        return new Disponibilidade(data, horarios);
    }

    /** Morador/proprietário: própria unidade; portaria: as do dia (padrão hoje); síndico: todas. */
    @Transactional(readOnly = true)
    public ReservaPage buscarReservas(UsuarioLogado usuario, LocalDate data, int page, int size) {
        Long unidadeId = usuario.isMoradorOuProprietario() ? usuario.unidadeId() : null;
        LocalDate dia = (data == null && usuario.perfil() == Perfil.PORTARIA) ? LocalDate.now() : data;
        Instant inicio = dia == null ? null : horario(dia, 0);
        Instant fim = dia == null ? null : horario(dia.plusDays(1), 0);

        Page<Reserva> pagina = reservaRepository.buscarComFiltros(usuario.condominioId(), unidadeId, inicio, fim,
                PageRequest.of(page, size));
        return PageMetadata.paginar(pagina, br.com.morah.dto.reservas.Reserva::de, ReservaPage::new);
    }

    /** Nasce confirmada, com valor = taxa da área; unidade e solicitante vêm do token. */
    @Transactional
    public br.com.morah.dto.reservas.Reserva criarReserva(UsuarioLogado usuario, ReservaCreateRequest request) {
        AreaComum area = buscarArea(usuario, request.areaComumId());
        if (reservaRepository.existsByAreaComumIdAndStatusAndInicioLessThanAndFimGreaterThan(
                area.getId(), StatusReserva.CONFIRMADA, request.fim(), request.inicio())) {
            throw ApiException.conflito("Horário indisponível: já existe reserva confirmada nesse período");
        }

        Reserva reserva = new Reserva();
        reserva.setAreaComum(area);
        reserva.setUnidade(unidadeRepository.findById(usuario.unidadeId()).orElseThrow());
        reserva.setSolicitante(pessoaRepository.findById(usuario.pessoaId()).orElseThrow());
        reserva.setInicio(request.inicio());
        reserva.setFim(request.fim());
        reserva.setStatus(StatusReserva.CONFIRMADA);
        reserva.setValor(area.getTaxa());
        reservaRepository.save(reserva);

        notificacaoReserva.notificar(reserva, List.of(reserva.getSolicitante()));
        return br.com.morah.dto.reservas.Reserva.de(reserva);
    }

    @Transactional(readOnly = true)
    public br.com.morah.dto.reservas.Reserva buscarReserva(UsuarioLogado usuario, Long reservaId) {
        return br.com.morah.dto.reservas.Reserva.de(buscarDoCondominio(usuario, reservaId));
    }

    /** A reserva não é apagada: passa a "cancelada". */
    @Transactional
    public br.com.morah.dto.reservas.Reserva deletarReserva(UsuarioLogado usuario, Long reservaId) {
        Reserva reserva = buscarDoCondominio(usuario, reservaId);
        if (reserva.getStatus() != StatusReserva.CONFIRMADA) {
            throw ApiException.conflito("A reserva não está mais confirmada");
        }
        reserva.setStatus(StatusReserva.CANCELADA);
        return br.com.morah.dto.reservas.Reserva.de(reserva);
    }

    /** Dano gera ocorrência aberta; vistoria de saída conclui a reserva. */
    @Transactional
    public br.com.morah.dto.reservas.Reserva registrarVistoria(UsuarioLogado usuario, Long reservaId, VistoriaRequest request) {
        Reserva reserva = buscarDoCondominio(usuario, reservaId);
        boolean dano = Boolean.TRUE.equals(request.dano());

        Vistoria vistoria = new Vistoria();
        vistoria.setReserva(reserva);
        vistoria.setMomento(request.momento());
        vistoria.setObservacao(request.observacao());
        vistoria.setDano(dano);
        if (request.fotosUrl() != null) {
            vistoria.setFotosUrl(new ArrayList<>(request.fotosUrl()));
        }
        vistoria.setRegistradaEm(Instant.now());
        vistoriaRepository.save(vistoria);

        if (dano) {
            Ocorrencia ocorrencia = Ocorrencia.aberta(reserva.getAreaComum().getCondominio(),
                    "Dano em " + reserva.getAreaComum().getNome(),
                    request.observacao() == null ? "Dano registrado na vistoria" : request.observacao());
            ocorrencia.setReserva(reserva);
            ocorrenciaRepository.save(ocorrencia);
        }
        if (request.momento() == MomentoVistoria.SAIDA) {
            reserva.setStatus(StatusReserva.CONCLUIDA);
        }
        return br.com.morah.dto.reservas.Reserva.de(reserva);
    }

    /** Converte "data + hora cheia" (fuso do servidor) em instante. */
    public static Instant horario(LocalDate data, int hora) {
        return data.atStartOfDay(ZoneId.systemDefault()).plusHours(hora).toInstant();
    }

    private AreaComum buscarArea(UsuarioLogado usuario, Long areaComumId) {
        return areaComumRepository.findByIdAndCondominioId(areaComumId, usuario.condominioId())
                .orElseThrow(() -> ApiException.naoEncontrado("Área comum não encontrada"));
    }

    private Reserva buscarDoCondominio(UsuarioLogado usuario, Long reservaId) {
        return reservaRepository.findByIdAndAreaComumCondominioId(reservaId, usuario.condominioId())
                .orElseThrow(() -> ApiException.naoEncontrado("Reserva não encontrada"));
    }
}
