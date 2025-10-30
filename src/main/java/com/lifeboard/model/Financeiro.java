package com.lifeboard.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "lb_financeiros")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Financeiro {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "lb_financeiro_seq")
    @SequenceGenerator(name = "lb_financeiro_seq", sequenceName = "LB_financeiro_SEQ", allocationSize = 1)
    @Column(name = "id_financeiro")
    @JsonProperty("id_financeiro")
    private Long id;

    @Column(name = "saldo_atual", nullable = false)
    private BigDecimal saldoAtual;

    @Column(name = "salario", nullable = false)
    private BigDecimal salarioMensal;

    @OneToOne
    @JoinColumn(name = "id_usuario", referencedColumnName = "id_usuario", nullable = false)
    private Usuario usuario;

    @OneToMany(mappedBy = "financeiro", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transacao> transacoes;

    @OneToMany(mappedBy = "financeiro", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MetaFinanceira> metas;
}
