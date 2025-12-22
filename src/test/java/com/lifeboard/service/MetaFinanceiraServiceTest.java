package com.lifeboard.service;

import com.lifeboard.dto.financeiro.FinanceiroRequestDTO;
import com.lifeboard.dto.meta.MetaFinanceiraSaveRequestDTO;
import com.lifeboard.dto.meta.MetaFinanceiraUpdateRequestDTO;
import com.lifeboard.exception.BadRequestException;
import com.lifeboard.model.Financeiro;
import com.lifeboard.model.MetaFinanceira;
import com.lifeboard.model.Transacao;
import com.lifeboard.model.Usuario;
import com.lifeboard.model.enums.StatusMeta;
import com.lifeboard.repository.MetaFinanceiraRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MetaFinanceiraServiceTest {

    @InjectMocks
    private MetaFinanceiraService service;

    @Mock
    private MetaFinanceiraRepository metaRepository;

    @Mock
    private FinanceiroService financeiroService;

    @Mock
    private TransacaoService transacaoService;

    private Financeiro financeiro;
    private Usuario usuario;
    private MetaFinanceira meta;

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

        meta = new MetaFinanceira();
        meta.setId(1L);
        meta.setNome("Viagem");
        meta.setFinanceiro(financeiro);
        meta.setValorMeta(BigDecimal.valueOf(2000));
        meta.setValorAtual(BigDecimal.valueOf(500));
    }

    @Test
    void deveCriarMetaDescontandoDoSaldo() {
        MetaFinanceiraSaveRequestDTO dto = new MetaFinanceiraSaveRequestDTO();
        dto.setIdFinanceiro(1L);
        dto.setValorMeta(BigDecimal.valueOf(2000));
        dto.setValorAtual(BigDecimal.valueOf(300));

        when(financeiroService.buscarEntidadePorId(1L)).thenReturn(financeiro);
        when(metaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.salvar(dto);

        assertEquals(BigDecimal.valueOf(700), financeiro.getSaldoAtual());
        verify(metaRepository).save(any(MetaFinanceira.class));
        verify(financeiroService).atualizar(eq(1L), any(FinanceiroRequestDTO.class));
    }

    @Test
    void naoDeveCriarMetaQuandoSaldoInsuficiente() {
        MetaFinanceiraSaveRequestDTO dto = new MetaFinanceiraSaveRequestDTO();
        dto.setIdFinanceiro(1L);
        dto.setValorAtual(BigDecimal.valueOf(2000));

        when(financeiroService.buscarEntidadePorId(1L)).thenReturn(financeiro);

        assertThrows(BadRequestException.class, () -> service.salvar(dto));
    }

    @Test
    void deveAdicionarSaldoNaMetaCriandoTransacaoEAtualizandoFinanceiro() {
        when(metaRepository.findById(1L)).thenReturn(Optional.of(meta));

        service.adicionarSaldo(1L, BigDecimal.valueOf(200));

        assertEquals(BigDecimal.valueOf(800), financeiro.getSaldoAtual());
        assertEquals(BigDecimal.valueOf(700), meta.getValorAtual());

        verify(transacaoService).salvar(any());
        verify(financeiroService).atualizar(eq(1L), any());
        verify(metaRepository).save(meta);
    }

    @Test
    void naoDeveAdicionarSaldoQuandoFinanceiroNaoTemSaldo() {
        financeiro.setSaldoAtual(BigDecimal.valueOf(100));

        when(metaRepository.findById(1L)).thenReturn(Optional.of(meta));

        assertThrows(BadRequestException.class,
                () -> service.adicionarSaldo(1L, BigDecimal.valueOf(500)));
    }

    @Test
    void deveRetirarSaldoDaMetaCriandoTransacaoEAtualizandoFinanceiro() {
        when(metaRepository.findById(1L)).thenReturn(Optional.of(meta));

        service.retirarSaldo(1L, BigDecimal.valueOf(200));

        assertEquals(BigDecimal.valueOf(300), meta.getValorAtual());
        assertEquals(BigDecimal.valueOf(1200), financeiro.getSaldoAtual());

        verify(transacaoService).salvar(any());
        verify(financeiroService).atualizar(eq(1L), any());
        verify(metaRepository).save(meta);
    }

    @Test
    void naoDeveRetirarSaldoMaiorQueMeta() {
        when(metaRepository.findById(1L)).thenReturn(Optional.of(meta));

        assertThrows(BadRequestException.class,
                () -> service.retirarSaldo(1L, BigDecimal.valueOf(1000)));
    }

    @Test
    void deveAtualizarMetaEAlterarDescricoesDasTransacoes() {
        Transacao t1 = new Transacao();
        t1.setDescricao("Aplicação na meta: Viagem");

        Transacao t2 = new Transacao();
        t2.setDescricao("Retirada da meta: Viagem");

        meta.setId(1L);

        when(metaRepository.findById(1L)).thenReturn(Optional.of(meta));

        when(transacaoService.buscarTransacoesPorFinanceiro(financeiro))
                .thenReturn(List.of(t1, t2));

        when(metaRepository.save(any())).thenReturn(meta);

        MetaFinanceiraUpdateRequestDTO dto = new MetaFinanceiraUpdateRequestDTO();
        dto.setNome("Europa");
        dto.setValorMeta(BigDecimal.valueOf(4000));

        service.atualizar(1L, dto);

        assertEquals("Europa", meta.getNome());
        assertEquals("Aplicação na meta: Europa", t1.getDescricao());
        assertEquals("Retirada da meta: Europa", t2.getDescricao());

        verify(transacaoService).atualizarVariasTransacoes(any());
        verify(metaRepository).save(meta);
    }

    @Test
    void deveExcluirMetaDevolvendoSaldoEApagandoTransacoesRelacionadas() {
        Transacao t1 = new Transacao();
        t1.setDescricao("Aplicação na meta: Viagem");

        when(metaRepository.findById(1L)).thenReturn(Optional.of(meta));
        when(transacaoService.buscarTransacoesPorFinanceiro(financeiro))
                .thenReturn(List.of(t1));

        service.deletar(1L);

        assertEquals(BigDecimal.valueOf(1500), financeiro.getSaldoAtual());
        verify(transacaoService).deletarVariasTransacoes(any());
        verify(metaRepository).deleteById(1L);
    }

    @Test
    void deveMarcarMetaComoConcluida() {
        meta.setValorAtual(BigDecimal.valueOf(2000));
        meta.setValorMeta(BigDecimal.valueOf(1500));

        service.definirStatusMetaFinanceiro(meta);

        assertEquals(StatusMeta.CONCLUIDA, meta.getStatus());
    }

    @Test
    void deveMarcarMetaComoEmAndamento() {
        meta.setValorAtual(BigDecimal.valueOf(500));
        meta.setValorMeta(BigDecimal.valueOf(1500));

        service.definirStatusMetaFinanceiro(meta);

        assertEquals(StatusMeta.EM_ANDAMENTO, meta.getStatus());
    }
}