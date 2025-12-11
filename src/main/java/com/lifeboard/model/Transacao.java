package com.lifeboard.model;

import com.lifeboard.model.enums.CategoriaTransacao;
import com.lifeboard.model.enums.TipoTransacao;
import jakarta.persistence.Entity;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "lb_transacoes")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Transacao {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "lb_transacao_seq")
    @SequenceGenerator(name = "lb_transacao_seq", sequenceName = "LB_TRANSACAO_SEQ", allocationSize = 1)
    @Column(name = "id_transacao")
    private Long id;

    @Column(nullable = false, length = 100)
    private String descricao;

    @Column(nullable = false)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private TipoTransacao tipo;

    @Column(nullable = false)
    private LocalDateTime data;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private CategoriaTransacao categoria;

    @ManyToOne
    @JoinColumn(name = "id_financeiro", nullable = false)
    private Financeiro financeiro;

    @PrePersist
    protected void onCreate() {
        this.data = LocalDateTime.now();
    }
}
