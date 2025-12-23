package com.lifeboard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifeboard.dto.usuario.UsuarioRequestDTO;
import com.lifeboard.dto.usuario.UsuarioResponseDTO;
import com.lifeboard.security.SecurityFilter;
import com.lifeboard.service.TokenService;
import com.lifeboard.service.UsuarioService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsuarioController.class)
@AutoConfigureMockMvc(addFilters = false)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private SecurityFilter securityFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Deve retornar página de usuários")
    void listarTodos() throws Exception {
        var usuario = new UsuarioResponseDTO(
                1L,
                "Felipe",
                "felipe@test.com",
                "123456",
                null,
                List.of()
        );
        var page = new PageImpl<>(List.of(usuario), PageRequest.of(0, 10), 1);

        Mockito.when(usuarioService.listarTodos(any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/usuarios?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id_usuario").value(1))
                .andExpect(jsonPath("$.content[0].nome").value("Felipe"));
    }

    @Test
    @DisplayName("Deve buscar usuário por id")
    void buscarPorId() throws Exception {
        var usuario = new UsuarioResponseDTO(
                1L,
                "Felipe",
                "felipe@test.com",
                "123456",
                null,
                List.of()
        );

        Mockito.when(usuarioService.buscarUsuarioDtoPorId(1L))
                .thenReturn(usuario);

        mockMvc.perform(get("/api/usuarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id_usuario").value(1))
                .andExpect(jsonPath("$.nome").value("Felipe"));
    }

    @Test
    @DisplayName("Deve criar um usuário")
    void salvar() throws Exception {
        var request = new UsuarioRequestDTO("Felipe", "felipe@test.com", "123456");
        var response = new UsuarioResponseDTO(
                1L,
                "Felipe",
                "felipe@test.com",
                "123456",
                null,
                List.of()
        );

        Mockito.when(usuarioService.salvar(any())).thenReturn(response);

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id_usuario").value(1));
    }

    @Test
    @DisplayName("Deve atualizar usuário")
    void atualizar() throws Exception {
        var request = new UsuarioRequestDTO("Atualizado", "update@test.com", "123456");
        var response = new UsuarioResponseDTO(
                1L,
                "Atualizado",
                "update@test.com",
                "123456",
                null,
                List.of()
        );

        Mockito.when(usuarioService.atualizar(eq(1L), any()))
                .thenReturn(response);

        mockMvc.perform(put("/api/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Atualizado"));
    }

    @Test
    @DisplayName("Deve deletar usuário")
    void deletar() throws Exception {
        Mockito.doNothing().when(usuarioService).deletar(1L);

        mockMvc.perform(delete("/api/usuarios/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deve enviar foto de perfil")
    void atualizarFoto() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "foto.png",
                MediaType.IMAGE_PNG_VALUE,
                "imagemfake".getBytes()
        );

        Mockito.doNothing().when(usuarioService).atualizarFotoPerfil(eq(1L), any());

        mockMvc.perform(multipart("/api/usuarios/1/foto")
                        .file(file)
                        .with(request -> { request.setMethod("PUT"); return request; })) // Multipart default é POST
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve retornar foto de perfil")
    void buscarFoto() throws Exception {
        byte[] img = "imagem".getBytes();

        Mockito.when(usuarioService.buscarFotoPerfil(1L))
                .thenReturn(img);

        mockMvc.perform(get("/api/usuarios/1/foto"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(content().bytes(img));
    }

    @Test
    @DisplayName("Deve remover foto de perfil")
    void removerFoto() throws Exception {
        Mockito.doNothing().when(usuarioService).removerFotoPerfil(1L);

        mockMvc.perform(delete("/api/usuarios/1/foto"))
                .andExpect(status().isOk());
    }
}