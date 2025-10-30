package com.lifeboard.dto.usuario;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.lifeboard.dto.financeiro.FinanceiroResponseDTO;
import com.lifeboard.dto.TarefaResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder({ "id_usuario", "nome", "email", "senha", "financeiro", "tarefas"})
public class UsuarioResponseDTO {

    @JsonProperty("id_usuario")
    private Long id;

    private String nome;

    private String email;

    private String senha;

    private FinanceiroResponseDTO financeiro;

    private List<TarefaResponseDTO> tarefas;
}
