package com.lifeboard.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lifeboard.model.enums.Prioridade;
import com.lifeboard.model.enums.StatusTarefa;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "lb_tarefas")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Tarefa {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "lb_tarefa_seq")
    @SequenceGenerator(name = "lb_tarefa_seq", sequenceName = "LB_tarefa_SEQ", allocationSize = 1)
    @Column(name = "id_tarefa")
    @JsonProperty("id_tarefa")
    private Long id;

    @Column(nullable = false, length = 50)
    private String titulo;

    @Column(nullable = false, length = 100)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Prioridade prioridade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusTarefa status;

    @Column(name = "data_limite", nullable = false)
    private LocalDate dataLimite;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
}
