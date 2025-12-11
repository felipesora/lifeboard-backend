package com.lifeboard.mapper;

import com.lifeboard.dto.transacao.TransacaoRequestDTO;
import com.lifeboard.dto.transacao.TransacaoResponseDTO;
import com.lifeboard.model.Financeiro;
import com.lifeboard.model.Transacao;

public class TransacaoMapper {

    public static TransacaoResponseDTO toDTO(Transacao transacao) {
        return new TransacaoResponseDTO(
                transacao.getId(),
                transacao.getDescricao(),
                transacao.getValor(),
                transacao.getTipo(),
                transacao.getData(),
                transacao.getCategoria(),
                transacao.getFinanceiro().getId()
        );
    }

    public static Transacao toEntity(TransacaoRequestDTO dto, Financeiro financeiro) {
        Transacao transacao = new Transacao();
        transacao.setDescricao(dto.getDescricao());
        transacao.setValor(dto.getValor());
        transacao.setTipo(dto.getTipo());
        transacao.setCategoria(dto.getCategoria());
        transacao.setFinanceiro(financeiro);
        return transacao;
    }
}
