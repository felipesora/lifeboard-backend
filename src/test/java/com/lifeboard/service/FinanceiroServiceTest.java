package com.lifeboard.service;

import com.lifeboard.dto.financeiro.FinanceiroRequestDTO;
import com.lifeboard.model.Financeiro;
import com.lifeboard.model.Usuario;
import com.lifeboard.repository.FinanceiroRepository;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FinanceiroServiceTest {

    @InjectMocks
    private FinanceiroService service;

    @Mock
    private FinanceiroRepository repository;

    @Mock
    private UsuarioService usuarioService;

    private Financeiro financeiro;
    private Usuario usuario;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        usuario = new Usuario();
        usuario.setId(1L);

        financeiro = new Financeiro();
        financeiro.setId(1L);
        financeiro.setSaldoAtual(new BigDecimal("1000.00"));
        financeiro.setSalarioMensal(new BigDecimal("5000.00"));
        financeiro.setUsuario(usuario);
    }

    @Test
    void deveListarFinanceiros() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Financeiro> page = new PageImpl<>(List.of(financeiro));

        when(repository.findAllByOrderByIdAsc(pageable)).thenReturn(page);

        var result = service.listarTodos(pageable);

        assertEquals(1, result.getTotalElements());
        verify(repository, times(1)).findAllByOrderByIdAsc(pageable);
    }

    @Test
    void deveBuscarPorIdComSucesso() {
        when(repository.findById(1L)).thenReturn(Optional.of(financeiro));

        var result = service.buscarDTOPorId(1L);

        assertEquals(financeiro.getSaldoAtual(), result.getSaldoAtual());
        verify(repository).findById(1L);
    }

    @Test
    void deveLancarErroQuandoNaoEncontrarId() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.buscarDTOPorId(99L));
    }

    @Test
    void deveSalvarFinanceiro() {
        FinanceiroRequestDTO dto = new FinanceiroRequestDTO();
        dto.setUsuarioId(1L);
        dto.setSaldoAtual(new BigDecimal("2000.00"));
        dto.setSalarioMensal(new BigDecimal("6000.00"));

        when(usuarioService.buscarEntidadePorId(1L)).thenReturn(usuario);
        when(repository.save(any(Financeiro.class))).thenReturn(financeiro);

        var result = service.salvar(dto);

        assertNotNull(result);
        verify(usuarioService).buscarEntidadePorId(1L);
        verify(repository).save(any(Financeiro.class));
    }

    @Test
    void deveAtualizarFinanceiro() {
        FinanceiroRequestDTO dto = new FinanceiroRequestDTO();
        dto.setUsuarioId(1L);
        dto.setSaldoAtual(new BigDecimal("3000.00"));
        dto.setSalarioMensal(new BigDecimal("7000.00"));

        when(repository.findById(1L)).thenReturn(Optional.of(financeiro));
        when(usuarioService.buscarEntidadePorId(1L)).thenReturn(usuario);
        when(repository.save(any(Financeiro.class))).thenReturn(financeiro);

        var result = service.atualizar(1L, dto);

        assertNotNull(result);
        verify(repository).save(financeiro);
    }

    @Test
    void deveDeletarFinanceiro() {
        when(repository.findById(1L)).thenReturn(Optional.of(financeiro));

        service.deletar(1L);

        verify(repository).delete(financeiro);
    }
}