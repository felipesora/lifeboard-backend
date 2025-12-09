package com.lifeboard.controller;

import com.lifeboard.dto.transacao.TransacaoRequestDTO;
import com.lifeboard.dto.transacao.TransacaoResponseDTO;
import com.lifeboard.mapper.TransacaoMapper;
import com.lifeboard.model.Financeiro;
import com.lifeboard.model.Transacao;
import com.lifeboard.service.FinanceiroService;
import com.lifeboard.service.TransacaoService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("api/transacoes")
@SecurityRequirement(name = "bearer-key")
public class TransacaoController {

    @Autowired
    private TransacaoService transacaoService;

    @Operation(summary = "Listar todas as transações", description = "Retorna uma página de transações com paginação e ordenação")
    @Parameters({
            @Parameter(name = "page", description = "Número da página (começa em 0)", example = "0"),
            @Parameter(name = "size", description = "Quantidade de elementos por página", example = "10"),
            @Parameter(name = "sort", description = "Campo para ordenação. Ex: nome,asc ou id,desc", example = "nome,asc")
    })
    @GetMapping
    public ResponseEntity<Page<TransacaoResponseDTO>> listarTodos(@PageableDefault(size = 10, page = 0, sort = {"id"}) Pageable paginacao) {
        var transacoes = transacaoService.listarTodos(paginacao);
        return ResponseEntity.ok(transacoes);
    }

    @Operation(summary = "Buscar transação por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transação encontrada"),
            @ApiResponse(responseCode = "404", description = "Transação não encontrada", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<TransacaoResponseDTO> buscarPorId(@PathVariable Long id) {
        var transacao = transacaoService.buscarDTOPorId(id);
        return ResponseEntity.ok(transacao);
    }

    @Operation(summary = "Cadastrar uma nova transação")
    @ApiResponse(responseCode = "201", description = "Transação criada com sucesso")
    @PostMapping
    public ResponseEntity<TransacaoResponseDTO> salvar(@RequestBody @Valid TransacaoRequestDTO transacaoDTO, UriComponentsBuilder uriBuilder){
        var transacaoSalva = transacaoService.salvar(transacaoDTO);

        var uri = uriBuilder.path("/api/transacoes/{id}").buildAndExpand(transacaoSalva.getId()).toUri();

        return ResponseEntity.created(uri).body(transacaoSalva);
    }

    @Operation(summary = "Atualizar uma transação existente")
    @PutMapping("/{id}")
    public ResponseEntity<TransacaoResponseDTO> atualizar(@PathVariable Long id, @RequestBody @Valid TransacaoRequestDTO transacaoDTO){
        var transacaoAtualizada = transacaoService.atualizar(id,transacaoDTO);
        return ResponseEntity.ok(transacaoAtualizada);
    }

    @Operation(summary = "Deletar uma transação")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id){
        transacaoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
