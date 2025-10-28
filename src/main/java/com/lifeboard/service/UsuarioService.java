package com.lifeboard.service;

import com.lifeboard.dto.usuario.UsuarioRequestDTO;
import com.lifeboard.dto.usuario.UsuarioResponseDTO;
import com.lifeboard.mapper.UsuarioMapper;
import com.lifeboard.model.Financeiro;
import com.lifeboard.model.Usuario;
import com.lifeboard.repository.FinanceiroRepository;
import com.lifeboard.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.math.BigDecimal;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private FinanceiroRepository financeiroRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Page<UsuarioResponseDTO> listarTodos(Pageable pageable) {
        return usuarioRepository.findAllByOrderByIdAsc(pageable)
                .map(UsuarioMapper::toDTO);
    }

    public UsuarioResponseDTO buscarUsuarioDtoPorId(Long id) {
        var usuario = buscarEntidadePorId(id);

        return UsuarioMapper.toDTO(usuario);
    }

    @Transactional
    public UsuarioResponseDTO salvar(UsuarioRequestDTO usuarioDTO) {
        criptografarSenhaSeNecessario(usuarioDTO);

        Usuario usuarioSalvo = usuarioRepository.save(UsuarioMapper.toEntity(usuarioDTO));

        if (usuarioSalvo.getFinanceiro() == null) {
            Financeiro financeiro = new Financeiro();
            financeiro.setSaldoAtual(BigDecimal.ZERO);
            financeiro.setSalarioMensal(BigDecimal.ZERO);
            financeiro.setUsuario(usuarioSalvo);

            financeiroRepository.save(financeiro);
        }

        return UsuarioMapper.toDTO(usuarioSalvo);
    }

    @Transactional
    public UsuarioResponseDTO atualizar(Long id, UsuarioRequestDTO usuarioDTO) {
        criptografarSenhaSeNecessario(usuarioDTO);
        Usuario usuarioExistente = buscarEntidadePorId(id);
        Usuario usuarioComNovosDados = UsuarioMapper.toEntity(usuarioDTO);

        usuarioExistente.setNome(usuarioComNovosDados.getNome());
        usuarioExistente.setEmail(usuarioComNovosDados.getEmail());

        if (usuarioComNovosDados.getSenha() != null && !usuarioComNovosDados.getSenha().isBlank()) {
            usuarioExistente.setSenha(usuarioComNovosDados.getSenha());
        }

        if (usuarioComNovosDados.getFinanceiro() != null) {
            usuarioExistente.setFinanceiro(usuarioComNovosDados.getFinanceiro());
        }

        var usuarioAtualizado = usuarioRepository.save(usuarioExistente);

        return UsuarioMapper.toDTO(usuarioAtualizado);
    }

    @Transactional
    public void deletar(Long id) {
        var usuario = buscarEntidadePorId(id);
        usuarioRepository.delete(usuario);
    }

    public void atualizarFotoPerfil(Long id, MultipartFile file) throws IOException {
        Usuario usuario = buscarEntidadePorId(id);

        usuario.setFotoPerfil(file.getBytes());
        usuarioRepository.save(usuario);
    }

    public byte[] buscarFotoPerfil(Long id) {
        Usuario usuario = buscarEntidadePorId(id);

        return usuario.getFotoPerfil();
    }

    public void removerFotoPerfil(Long id) {
        Usuario usuario = buscarEntidadePorId(id);

        usuario.setFotoPerfil(null);
        usuarioRepository.save(usuario);
    }

    public Usuario buscarEntidadePorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário com id: " + id + " não encontrado"));
    }

    private void criptografarSenhaSeNecessario(UsuarioRequestDTO usuario) {
        if (usuario.getSenha() != null && !usuario.getSenha().isBlank()) {
            usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        }
    }
}
