package br.com.morah.model.minhaunidade;

import br.com.morah.dto.minhaunidade.TipoVinculo;
import br.com.morah.model.condominio.Unidade;
import br.com.morah.model.login.Pessoa;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/** Relação da pessoa com a unidade (proprietário, inquilino ou dependente) e o período dela. */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class VinculoUnidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Unidade unidade;

    @ManyToOne
    private Pessoa pessoa;

    @Enumerated(EnumType.STRING)
    private TipoVinculo tipoVinculo;

    private boolean principal;

    private LocalDate inicio;

    private LocalDate fim;
}
