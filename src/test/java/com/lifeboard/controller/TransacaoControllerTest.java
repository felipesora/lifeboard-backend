package com.lifeboard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifeboard.dto.transacao.TransacaoRequestDTO;
import com.lifeboard.dto.transacao.TransacaoResponseDTO;
import com.lifeboard.model.enums.CategoriaTransacao;
import com.lifeboard.model.enums.TipoTransacao;
import com.lifeboard.security.SecurityFilter;
import com.lifeboard.service.TokenService;
import com.lifeboard.service.TransacaoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(TransacaoController.class)
@AutoConfigureMockMvc(addFilters = false)
class TransacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransacaoService transacaoService;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private SecurityFilter securityFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Deve retornar página de transações")
    void listarTodos() throws Exception {
        var transacao = new TransacaoResponseDTO(
                1L,
                "Salário",
                new BigDecimal("2500.00"),
                TipoTransacao.ENTRADA,
                LocalDateTime.now(),
                CategoriaTransacao.SALARIO,
                5L
        );

        var page = new PageImpl<>(List.of(transacao), PageRequest.of(0, 10), 1);

        Mockito.when(transacaoService.listarTodos(any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/transacoes?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id_transacao").value(1))
                .andExpect(jsonPath("$.content[0].descricao").value("Salário"));
    }

    @Test
    @DisplayName("Deve buscar transação por id")
    void buscarPorId() throws Exception {
        var transacao = new TransacaoResponseDTO(
                1L,
                "Compra Mercado",
                new BigDecimal("120.50"),
                TipoTransacao.SAIDA,
                LocalDateTime.now(),
                CategoriaTransacao.ALIMENTACAO,
                5L
        );

        Mockito.when(transacaoService.buscarDTOPorId(1L))
                .thenReturn(transacao);

        mockMvc.perform(get("/api/transacoes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id_transacao").value(1))
                .andExpect(jsonPath("$.descricao").value("Compra Mercado"));
    }

    @Test
    @DisplayName("Deve criar transação")
    void salvar() throws Exception {
        var request = new TransacaoRequestDTO(
                "Internet",
                new BigDecimal("99.90"),
                TipoTransacao.SAIDA,
                CategoriaTransacao.MORADIA,
                5L
        );

        var response = new TransacaoResponseDTO(
                1L,
                "Internet",
                new BigDecimal("99.90"),
                TipoTransacao.SAIDA,
                LocalDateTime.now(),
                CategoriaTransacao.MORADIA,
                5L
        );

        Mockito.when(transacaoService.salvar(any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/transacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id_transacao").value(1));
    }

    @Test
    @DisplayName("Deve atualizar transação")
    void atualizar() throws Exception {
        var request = new TransacaoRequestDTO(
                "Conta de Luz",
                new BigDecimal("150.00"),
                TipoTransacao.SAIDA,
                CategoriaTransacao.MORADIA,
                5L
        );

        var response = new TransacaoResponseDTO(
                1L,
                "Conta de Luz",
                new BigDecimal("150.00"),
                TipoTransacao.SAIDA,
                LocalDateTime.now(),
                CategoriaTransacao.MORADIA,
                5L
        );

        Mockito.when(transacaoService.atualizar(eq(1L), any()))
                .thenReturn(response);

        mockMvc.perform(put("/api/transacoes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Conta de Luz"));
    }

    @Test
    @DisplayName("Deve deletar transação")
    void deletar() throws Exception {
        Mockito.doNothing().when(transacaoService).deletar(1L);

        mockMvc.perform(delete("/api/transacoes/1"))
                .andExpect(status().isNoContent());
    }
}