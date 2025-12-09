package com.lifeboard.model;

import com.lifeboard.model.enums.StatusMeta;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "lb_metas_financeiras")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MetaFinanceira {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "lb_meta_seq")
    @SequenceGenerator(name = "lb_meta_seq", sequenceName = "LB_META_SEQ", allocationSize = 1)
    @Column(name = "id_meta")
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(name = "valor_meta",nullable = false)
    private BigDecimal valorMeta;

    @Column(name = "valor_atual", nullable = false)
    private BigDecimal valorAtual;

    @Column(name = "data_limite",nullable = false)
    private LocalDate dataLimite;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusMeta status;

    @ManyToOne
    @JoinColumn(name = "id_financeiro", nullable = false)
    private Financeiro financeiro;
}
