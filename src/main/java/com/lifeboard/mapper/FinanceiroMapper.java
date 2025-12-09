package com.lifeboard.mapper;

import com.lifeboard.dto.financeiro.FinanceiroRequestDTO;
import com.lifeboard.dto.financeiro.FinanceiroResponseDTO;
import com.lifeboard.dto.meta.MetaFinanceiraResponseDTO;
import com.lifeboard.dto.transacao.TransacaoResponseDTO;
import com.lifeboard.model.Financeiro;
import com.lifeboard.model.Usuario;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FinanceiroMapper {

    public static FinanceiroResponseDTO toDTO (Financeiro financeiro) {
        List<TransacaoResponseDTO> transacoes;
        List<MetaFinanceiraResponseDTO> metas;

        if (financeiro.getTransacoes() != null){
            transacoes = financeiro.getTransacoes()
                    .stream()
                    .map(TransacaoMapper::toDTO)
                    .collect(Collectors.toList());
        } else {
            transacoes = new ArrayList<>();
        }

        if (financeiro.getMetas() != null){
            metas = financeiro.getMetas()
                    .stream()
                    .map(MetaFinanceiraMapper::toDTO)
                    .collect(Collectors.toList());
        } else {
            metas = new ArrayList<>();
        }

        return new FinanceiroResponseDTO(
                financeiro.getId(),
                financeiro.getSaldoAtual(),
                financeiro.getSalarioMensal(),
                financeiro.getUsuario().getId(),
                transacoes,
                metas
        );
    }

    public static Financeiro toEntity(FinanceiroRequestDTO dto, Usuario usuario) {
        Financeiro financeiro = new Financeiro();

        financeiro.setSaldoAtual(dto.getSaldoAtual());
        financeiro.setSalarioMensal(dto.getSalarioMensal());
        financeiro.setUsuario(usuario);

        return financeiro;
    }
}
