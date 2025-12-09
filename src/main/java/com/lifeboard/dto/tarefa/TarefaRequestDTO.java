package com.lifeboard.dto.tarefa;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lifeboard.model.enums.Prioridade;
import com.lifeboard.model.enums.StatusTarefa;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TarefaRequestDTO {

    @NotBlank(message = "Titulo é obrigatória.")
    private String titulo;

    @NotBlank(message = "Descrição é obrigatória.")
    private String descricao;

    @NotNull(message = "Prioridade é obrigatória.")
    private Prioridade prioridade;

    @NotNull(message = "Status é obrigatório.")
    private StatusTarefa status;

    @NotNull(message = "Data Limite é obrigatória.")
    @JsonProperty("data_limite")
    private LocalDate dataLimite;

    @NotNull(message = "O ID do Usuário é obrigatório.")
    @JsonProperty("id_usuario")
    private Long usuarioId;
}
