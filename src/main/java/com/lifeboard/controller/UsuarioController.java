package com.lifeboard.controller;

import com.lifeboard.dto.usuario.UsuarioRequestDTO;
import com.lifeboard.dto.usuario.UsuarioResponseDTO;
import com.lifeboard.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@RestController
@RequestMapping("api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService service;

    @Operation(summary = "Listar todos os usuários", description = "Retorna uma página de usuários com paginação e ordenação")
    @Parameters({
            @Parameter(name = "page", description = "Número da página (começa em 0)", example = "0"),
            @Parameter(name = "size", description = "Quantidade de elementos por página", example = "10"),
            @Parameter(name = "sort", description = "Campo para ordenação. Ex: nome,asc ou id,desc", example = "nome,asc")
    })
    @SecurityRequirement(name = "bearer-key")
    @GetMapping
    public ResponseEntity<Page<UsuarioResponseDTO>> listarTodos(@PageableDefault(size = 10, page = 0, sort = {"id"}) Pageable paginacao) {
        var usuarios = service.listarTodos(paginacao);
        return ResponseEntity.ok(usuarios);
    }

    @Operation(summary = "Buscar usuário por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content)
    })
    @SecurityRequirement(name = "bearer-key")
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable Long id) {
        var usuario = service.buscarUsuarioDtoPorId(id);
        return ResponseEntity.ok(usuario);
    }

    @Operation(summary = "Cadastrar um novo usuário")
    @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso")
    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> salvar(@RequestBody @Valid UsuarioRequestDTO usuarioDto, UriComponentsBuilder uriBuilder) {
        var usuario = service.salvar(usuarioDto);

        var uri = uriBuilder.path("/api/usuarios/{id}").buildAndExpand(usuario.getId()).toUri();

        return ResponseEntity.created(uri).body(usuario);
    }

    @Operation(summary = "Atualizar um usuário existente")
    @SecurityRequirement(name = "bearer-key")
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> atualizar(@PathVariable Long id, @RequestBody @Valid UsuarioRequestDTO usuarioDto){
        var usuarioAtualizado = service.atualizar(id, usuarioDto);

        return ResponseEntity.ok(usuarioAtualizado);
    }

    @Operation(summary = "Deletar um usuário")
    @SecurityRequirement(name = "bearer-key")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id){
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Atualizar foto de perfil do usuário")
    @SecurityRequirement(name = "bearer-key")
    @PutMapping("/{id}/foto")
    public ResponseEntity<Void> atualizarFoto(@PathVariable Long id, @RequestParam("file") MultipartFile file) throws IOException {
        service.atualizarFotoPerfil(id, file);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Buscar foto de perfil do usuário", description = "Retorna a imagem PNG da foto de perfil")
    @SecurityRequirement(name = "bearer-key")
    @GetMapping(value = "/{id}/foto", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> buscarFoto(@PathVariable Long id) {
        byte[] imagem = service.buscarFotoPerfil(id);
        return ResponseEntity.ok(imagem);
    }

    @Operation(summary = "Remover foto de perfil do usuário")
    @SecurityRequirement(name = "bearer-key")
    @DeleteMapping("/{id}/foto")
    public ResponseEntity<Void> removerFoto(@PathVariable Long id) {
        service.removerFotoPerfil(id);
        return ResponseEntity.ok().build();
    }
}
