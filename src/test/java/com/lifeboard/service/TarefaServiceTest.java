package com.lifeboard.service;

import com.lifeboard.dto.tarefa.TarefaRequestDTO;
import com.lifeboard.model.Tarefa;
import com.lifeboard.model.Usuario;
import com.lifeboard.repository.TarefaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TarefaServiceTest {

    @Mock
    private TarefaRepository tarefaRepository;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private TarefaService tarefaService;

    private Tarefa tarefa;
    private Usuario usuario;
    private TarefaRequestDTO request;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        usuario = new Usuario();
        usuario.setId(1L);

        tarefa = new Tarefa();
        tarefa.setId(1L);
        tarefa.setTitulo("Estudar");
        tarefa.setDescricao("Estudar Spring Boot");
        tarefa.setUsuario(usuario);

        request = new TarefaRequestDTO();
        request.setTitulo("Atualizado");
        request.setDescricao("Nova desc");
        request.setUsuarioId(1L);
        request.setDataLimite(LocalDate.now());
    }

    @Test
    void deveListarTarefasComPaginacao() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Tarefa> page = new PageImpl<>(java.util.List.of(tarefa));

        when(tarefaRepository.findAllByOrderByIdAsc(pageable)).thenReturn(page);

        var result = tarefaService.listarTodos(pageable);

        assertEquals(1, result.getTotalElements());
        verify(tarefaRepository).findAllByOrderByIdAsc(pageable);
    }

    @Test
    void deveBuscarTarefaPorIdERetornarDTO() {
        when(tarefaRepository.findById(1L)).thenReturn(Optional.of(tarefa));

        var dto = tarefaService.buscarDTOPorId(1L);

        assertEquals("Estudar", dto.getTitulo());
        verify(tarefaRepository).findById(1L);
    }

    @Test
    void deveLancarExcecaoAoBuscarIdInexistente() {
        when(tarefaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> tarefaService.buscarDTOPorId(1L));
    }

    @Test
    void deveSalvarTarefaCorretamente() {
        when(usuarioService.buscarEntidadePorId(1L)).thenReturn(usuario);
        when(tarefaRepository.save(any())).thenReturn(tarefa);

        var dto = tarefaService.salvar(request);

        assertEquals("Estudar", dto.getTitulo());
        verify(usuarioService).buscarEntidadePorId(1L);
        verify(tarefaRepository).save(any());
    }

    @Test
    void deveAtualizarTarefaComSucesso() {
        when(tarefaRepository.findById(1L)).thenReturn(Optional.of(tarefa));
        when(usuarioService.buscarEntidadePorId(1L)).thenReturn(usuario);
        when(tarefaRepository.save(any())).thenReturn(tarefa);

        var dto = tarefaService.atualizar(1L, request);

        assertEquals("Atualizado", tarefa.getTitulo());
        assertEquals("Nova desc", tarefa.getDescricao());
        verify(tarefaRepository).save(tarefa);
    }

    @Test
    void deveDeletarTarefa() {
        when(tarefaRepository.findById(1L)).thenReturn(Optional.of(tarefa));

        tarefaService.deletar(1L);

        verify(tarefaRepository).delete(tarefa);
    }

    @Test
    void deveLancarErroAoDeletarIdInexistente() {
        when(tarefaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> tarefaService.deletar(1L));
    }
}