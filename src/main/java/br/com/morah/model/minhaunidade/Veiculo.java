package br.com.morah.model.minhaunidade;

import br.com.morah.dto.minhaunidade.TipoVeiculo;
import br.com.morah.model.condominio.Unidade;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Database First: mapeada a partir da tabela "veiculo" do schema.sql
 * (IntelliJ: Persistence → Generate Persistence Mapping → By Database Schema).
 * Nomes de tabela e colunas explícitos: quem manda é o banco, não a convenção do Hibernate.
 */
@Entity
@Table(name = "veiculo")
@Getter
@Setter
@NoArgsConstructor
public class Veiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "unidade_id", nullable = false)
    private Unidade unidade;

    @Column(name = "placa", nullable = false, length = 7, unique = true)
    private String placa;

    @Column(name = "modelo", nullable = false, length = 60)
    private String modelo;

    @Column(name = "cor", length = 30)
    private String cor;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 10)
    private TipoVeiculo tipo;
}
