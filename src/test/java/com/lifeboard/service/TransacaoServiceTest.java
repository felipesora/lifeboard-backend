package com.lifeboard.service;

import com.lifeboard.dto.transacao.TransacaoRequestDTO;
import com.lifeboard.exception.BadRequestException;
import com.lifeboard.model.Financeiro;
import com.lifeboard.model.Transacao;
import com.lifeboard.model.Usuario;
import com.lifeboard.model.enums.TipoTransacao;
import com.lifeboard.repository.MetaFinanceiraRepository;
import com.lifeboard.repository.TransacaoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransacaoServiceTest {

    @InjectMocks
    private TransacaoService service;

    @Mock
    private TransacaoRepository transacaoRepository;

    @Mock
    private FinanceiroService financeiroService;

    @Mock
    private MetaFinanceiraRepository metaFinanceiraRepository;

    private Financeiro financeiro;
    private Usuario usuario;
    private Transacao transacao;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        usuario = new Usuario();
        usuario.setId(1L);

        financeiro = new Financeiro();
        financeiro.setId(1L);
        financeiro.setSaldoAtual(BigDecimal.valueOf(1000));
        financeiro.setSalarioMensal(BigDecimal.valueOf(5000));
        financeiro.setUsuario(usuario);
        financeiro.setTransacoes(new ArrayList<>());

        transacao = new Transacao();
        transacao.setId(1L);
        transacao.setFinanceiro(financeiro);
        transacao.setValor(BigDecimal.valueOf(200));
        transacao.setTipo(TipoTransacao.SAIDA);
        transacao.setDescricao("Teste");
    }

    @Test
    void deveListarTransacoes() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Transacao> page = new PageImpl<>(List.of(transacao));

        when(transacaoRepository.findAllByOrderByIdAsc(pageable)).thenReturn(page);

        var result = service.listarTodos(pageable);

        assertEquals(1, result.getTotalElements());
        verify(transacaoRepository).findAllByOrderByIdAsc(pageable);
    }

    @Test
    void deveBuscarPorId() {
        when(transacaoRepository.findById(1L)).thenReturn(Optional.of(transacao));

        var dto = service.buscarDTOPorId(1L);

        assertEquals(transacao.getValor(), dto.getValor());
    }

    @Test
    void deveLancarErroAoBuscarInexistente() {
        when(transacaoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.buscarDTOPorId(99L));
    }

    @Test
    void deveSalvarEntradaEAumentarSaldo() {
        TransacaoRequestDTO dto = new TransacaoRequestDTO();
        dto.setIdFinanceiro(1L);
        dto.setTipo(TipoTransacao.ENTRADA);
        dto.setValor(BigDecimal.valueOf(300));

        when(financeiroService.buscarEntidadePorId(1L)).thenReturn(financeiro);
        when(transacaoRepository.save(any(Transacao.class))).thenReturn(transacao);

        var result = service.salvar(dto);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(1300), financeiro.getSaldoAtual());

        verify(financeiroService).atualizar(eq(1L), any());
        verify(transacaoRepository).save(any(Transacao.class));
    }

    @Test
    void deveSalvarSaidaEReduzirSaldo() {
        TransacaoRequestDTO dto = new TransacaoRequestDTO();
        dto.setIdFinanceiro(1L);
        dto.setTipo(TipoTransacao.SAIDA);
        dto.setValor(BigDecimal.valueOf(200));

        when(financeiroService.buscarEntidadePorId(1L)).thenReturn(financeiro);
        when(transacaoRepository.save(any(Transacao.class))).thenReturn(transacao);

        var result = service.salvar(dto);

        assertEquals(BigDecimal.valueOf(800), financeiro.getSaldoAtual());
    }

    @Test
    void deveLancarErroQuandoSaldoInsuficienteNaSaida() {
        TransacaoRequestDTO dto = new TransacaoRequestDTO();
        dto.setIdFinanceiro(1L);
        dto.setTipo(TipoTransacao.SAIDA);
        dto.setValor(BigDecimal.valueOf(5000));

        when(financeiroService.buscarEntidadePorId(1L)).thenReturn(financeiro);

        assertThrows(BadRequestException.class,
                () -> service.salvar(dto));
    }
    @Test
    void deveAtualizarTransacaoRecalculandoSaldo() {
        when(transacaoRepository.findById(1L)).thenReturn(Optional.of(transacao));
        when(transacaoRepository.save(any())).thenReturn(transacao);

        TransacaoRequestDTO dto = new TransacaoRequestDTO();
        dto.setValor(BigDecimal.valueOf(100));
        dto.setTipo(TipoTransacao.SAIDA);
        dto.setDescricao("nova");

        var result = service.atualizar(1L, dto);

        assertNotNull(result);
        verify(transacaoRepository).save(any());
        verify(financeiroService).atualizar(eq(1L), any());
    }

    @Test
    void deveDeletarTransacaoSaidaRestaurandoSaldo() {
        when(transacaoRepository.findById(1L)).thenReturn(Optional.of(transacao));

        service.deletar(1L);

        assertEquals(BigDecimal.valueOf(1200), financeiro.getSaldoAtual());
        verify(transacaoRepository).delete(transacao);
    }
}