package com.lifeboard.service;

import com.lifeboard.dto.usuario.UsuarioRequestDTO;
import com.lifeboard.dto.usuario.UsuarioResponseDTO;
import com.lifeboard.model.Financeiro;
import com.lifeboard.model.Usuario;
import com.lifeboard.repository.FinanceiroRepository;
import com.lifeboard.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    UsuarioRepository usuarioRepository;

    @Mock
    FinanceiroRepository financeiroRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    UsuarioService usuarioService;

    @Test
    void deveListarUsuariosComPaginacao() {
        //ARRANGE
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Felipe");

        Pageable pageable = PageRequest.of(0, 10);
        Page<Usuario> page = new PageImpl<>(java.util.List.of(usuario));

        //ACT
        when(usuarioRepository.findAllByOrderByIdAsc(pageable)).thenReturn(page);

        var result = usuarioService.listarTodos(pageable);

        //ASSERT
        assertEquals(1, result.getTotalElements());
        verify(usuarioRepository).findAllByOrderByIdAsc(pageable);
    }

    @Test
    void deveBuscarUsuarioPorId() {
        //ARRANGE
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Felipe");

        //ACT
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        var result = usuarioService.buscarUsuarioDtoPorId(1L);

        //ASSERT
        assertEquals("Felipe", result.getNome());
    }

    @Test
    void deveLancarErroQuandoUsuarioNaoExistir() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                usuarioService.buscarUsuarioDtoPorId(1L)
        );
    }

    @Test
    void deveSalvarUsuarioECriarFinanceiroSeNaoExistir() {
        UsuarioRequestDTO dto = new UsuarioRequestDTO(
          "Felipe",
          "email@email.com",
          "123456"
        );

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Felipe");

        when(passwordEncoder.encode("123456")).thenReturn("senha-criptografada");
        when(usuarioRepository.save(any())).thenReturn(usuario);

        var result = usuarioService.salvar(dto);

        assertEquals("Felipe", result.getNome());
        verify(financeiroRepository).save(any(Financeiro.class));
    }

    @Test
    void deveAtualizarUsuario() {
        UsuarioRequestDTO dto = new UsuarioRequestDTO(
                "Novo Nome",
                "novo@email.com",
                ""
        );

        Usuario existente = new Usuario();
        existente.setId(1L);
        existente.setNome("Antigo");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(usuarioRepository.save(any())).thenReturn(existente);

        UsuarioResponseDTO result = usuarioService.atualizar(1L, dto);

        assertEquals("Novo Nome", result.getNome());
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void deveDeletarUsuario() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        usuarioService.deletar(1L);

        verify(usuarioRepository).delete(usuario);
    }

    @Test
    void deveRetornarFotoPerfil() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setFotoPerfil("abc".getBytes());

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        byte[] bytes = usuarioService.buscarFotoPerfil(1L);

        assertNotNull(bytes);
    }
}