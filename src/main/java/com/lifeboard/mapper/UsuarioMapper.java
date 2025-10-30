package com.lifeboard.mapper;

import com.lifeboard.dto.*;
import com.lifeboard.dto.financeiro.FinanceiroResponseDTO;
import com.lifeboard.dto.usuario.UsuarioRequestDTO;
import com.lifeboard.dto.usuario.UsuarioResponseDTO;
import com.lifeboard.model.Usuario;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UsuarioMapper {

    public static UsuarioResponseDTO toDTO(Usuario usuario) {
        FinanceiroResponseDTO financeiroDTO = null;
        List<TarefaResponseDTO> tarefas;

        if (usuario.getFinanceiro() != null) {
            financeiroDTO = FinanceiroMapper.toDTO(usuario.getFinanceiro());
        }

        if (usuario.getTarefas() != null) {
            tarefas = usuario.getTarefas().stream()
                    .map(TarefaMapper::toDTO)
                    .collect(Collectors.toList());
        } else {
            tarefas = new ArrayList<>();
        }

        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getSenha(),
                financeiroDTO,
                tarefas
        );
    }

    public static Usuario toEntity(UsuarioRequestDTO dto){
        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setSenha(dto.getSenha());

        return usuario;
    }
}
