package com.lifeboard.dto.tarefa;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.lifeboard.model.enums.Prioridade;
import com.lifeboard.model.enums.StatusTarefa;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder({ "id_tarefa", "titulo", "descricao", "prioridade", "status", "data_limite", "id_usuario"})
public class TarefaResponseDTO {

    @JsonProperty("id_tarefa")
    private Long id;

    private String titulo;

    private String descricao;

    private Prioridade prioridade;

    private StatusTarefa status;

    @JsonProperty("data_limite")
    private LocalDate dataLimite;

    @JsonProperty("id_usuario")
    private Long usuarioId;
}
