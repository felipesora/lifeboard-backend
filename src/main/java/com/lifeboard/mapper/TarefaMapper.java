package com.lifeboard.mapper;

import com.lifeboard.dto.tarefa.TarefaRequestDTO;
import com.lifeboard.dto.tarefa.TarefaResponseDTO;
import com.lifeboard.model.Tarefa;
import com.lifeboard.model.Usuario;

public class TarefaMapper {
    public static TarefaResponseDTO toDTO(Tarefa tarefa){

        return new TarefaResponseDTO(
                tarefa.getId(),
                tarefa.getTitulo(),
                tarefa.getDescricao(),
                tarefa.getPrioridade(),
                tarefa.getStatus(),
                tarefa.getDataLimite(),
                tarefa.getUsuario().getId()
        );
    }

    public static Tarefa toEntity(TarefaRequestDTO dto, Usuario usuario){
        Tarefa tarefa = new Tarefa();
        tarefa.setTitulo(dto.getTitulo());
        tarefa.setDescricao(dto.getDescricao());
        tarefa.setPrioridade(dto.getPrioridade());
        tarefa.setStatus(dto.getStatus());
        tarefa.setDataLimite(dto.getDataLimite());
        tarefa.setUsuario(usuario);

        return tarefa;
    }
}
