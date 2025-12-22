package com.lifeboard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifeboard.dto.financeiro.FinanceiroRequestDTO;
import com.lifeboard.dto.financeiro.FinanceiroResponseDTO;
import com.lifeboard.security.SecurityFilter;
import com.lifeboard.service.FinanceiroService;
import com.lifeboard.service.TokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FinanceiroController.class)
@AutoConfigureMockMvc(addFilters = false)
class FinanceiroControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FinanceiroService financeiroService;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private SecurityFilter securityFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Deve retornar página de financeiros")
    void listarTodos() throws Exception {
        var financeiro = new FinanceiroResponseDTO(
                1L,
                new BigDecimal("1000.00"),
                new BigDecimal("3000.00"),
                10L,
                null,
                null
        );

        var page = new PageImpl<>(List.of(financeiro), PageRequest.of(0, 10), 1);

        Mockito.when(financeiroService.listarTodos(any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/financeiros?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id_financeiro").value(1))
                .andExpect(jsonPath("$.content[0].saldo").value(300.0));
    }

    @Test
    @DisplayName("Deve buscar financeiro por id")
    void buscarPorId() throws Exception {
        var financeiro = new FinanceiroResponseDTO(
                1L,
                new BigDecimal("500.00"),
                new BigDecimal("200.00"),
                10L,
                null,
                null
        );

        Mockito.when(financeiroService.buscarDTOPorId(1L))
                .thenReturn(financeiro);

        mockMvc.perform(get("/api/financeiros/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id_financeiro").value(1))
                .andExpect(jsonPath("$.saldo").value(500.0));
    }

    @Test
    @DisplayName("Deve criar financeiro")
    void salvar() throws Exception {
        var request = new FinanceiroRequestDTO(new BigDecimal("500.00"), new BigDecimal("200.00"), 10L);
        var response = new FinanceiroResponseDTO(
                1L,
                new BigDecimal("500.00"),
                new BigDecimal("200.00"),
                10L,
                null,
                null
        );

        Mockito.when(financeiroService.salvar(any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/financeiros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id_financeiro").value(1));
    }

    @Test
    @DisplayName("Deve atualizar financeiro")
    void atualizar() throws Exception {
        var request = new FinanceiroRequestDTO(new BigDecimal("400.00"), new BigDecimal("200.00"), 10L);
        var response = new FinanceiroResponseDTO(
                1L,
                new BigDecimal("400.00"),
                new BigDecimal("200.00"),
                10L,
                null,
                null
        );

        Mockito.when(financeiroService.atualizar(eq(1L), any()))
                .thenReturn(response);

        mockMvc.perform(put("/api/financeiros/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo").value(400.0));
    }

    @Test
    @DisplayName("Deve deletar financeiro")
    void deletar() throws Exception {
        Mockito.doNothing().when(financeiroService).deletar(1L);

        mockMvc.perform(delete("/api/financeiros/1"))
                .andExpect(status().isNoContent());
    }
}