package com.lifeboard.service;

import com.lifeboard.dto.tarefa.TarefaRequestDTO;
import com.lifeboard.dto.tarefa.TarefaResponseDTO;
import com.lifeboard.mapper.TarefaMapper;
import com.lifeboard.model.Tarefa;
import com.lifeboard.repository.TarefaRepository;
import com.lifeboard.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class TarefaService {

    @Autowired
    private TarefaRepository tarefaRepository;

    @Autowired
    private UsuarioService usuarioService;

    public Page<TarefaResponseDTO> listarTodos(Pageable pageable) {
        return tarefaRepository.findAllByOrderByIdAsc(pageable)
                .map(TarefaMapper::toDTO);
    }

    public TarefaResponseDTO buscarDTOPorId(Long id) {
        var tarefa = buscarEntidadePorId(id);

        return TarefaMapper.toDTO(tarefa);
    }

    public TarefaResponseDTO salvar(TarefaRequestDTO tarefaDTO) {
        var usuario = usuarioService.buscarEntidadePorId(tarefaDTO.getUsuarioId());
        var tarefaSalva = tarefaRepository.save(TarefaMapper.toEntity(tarefaDTO, usuario));
        return TarefaMapper.toDTO(tarefaSalva);
    }

    public TarefaResponseDTO atualizar(Long id, TarefaRequestDTO tarefaDTO) {
        Tarefa tarefaExistente = buscarEntidadePorId(id);
        var usuario = usuarioService.buscarEntidadePorId(tarefaDTO.getUsuarioId());

        tarefaExistente.setTitulo(tarefaDTO.getTitulo());
        tarefaExistente.setDescricao(tarefaDTO.getDescricao());
        tarefaExistente.setPrioridade(tarefaDTO.getPrioridade());
        tarefaExistente.setStatus(tarefaDTO.getStatus());
        tarefaExistente.setDataLimite(tarefaDTO.getDataLimite());
        tarefaExistente.setUsuario(usuario);

        var tarefaAtualizada = tarefaRepository.save(tarefaExistente);

        return TarefaMapper.toDTO(tarefaAtualizada);
    }

    public void deletar(Long id) {
        var tarefa = buscarEntidadePorId(id);
        tarefaRepository.delete(tarefa);
    }

    public Tarefa buscarEntidadePorId(Long id) {
        return tarefaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tarefa com id: " + id + " não encontrada"));
    }
}
