package com.lifeboard.mapper;

import com.lifeboard.dto.meta.MetaFinanceiraSaveRequestDTO;
import com.lifeboard.dto.meta.MetaFinanceiraResponseDTO;
import com.lifeboard.dto.meta.MetaFinanceiraUpdateRequestDTO;
import com.lifeboard.model.Financeiro;
import com.lifeboard.model.MetaFinanceira;

public class MetaFinanceiraMapper {

    public static MetaFinanceiraResponseDTO toDTO(MetaFinanceira meta) {
        return new MetaFinanceiraResponseDTO(
                meta.getId(),
                meta.getNome(),
                meta.getValorMeta(),
                meta.getValorAtual(),
                meta.getDataLimite(),
                meta.getStatus(),
                meta.getFinanceiro().getId()
        );
    }

    public static MetaFinanceira toEntitySave(MetaFinanceiraSaveRequestDTO dto, Financeiro financeiro) {
        MetaFinanceira meta = new MetaFinanceira();
        meta.setNome(dto.getNome());
        meta.setValorMeta(dto.getValorMeta());
        meta.setValorAtual(dto.getValorAtual());
        meta.setDataLimite(dto.getDataLimite());
        meta.setFinanceiro(financeiro);
        return meta;
    }

    public static MetaFinanceira toEntityUpdate(MetaFinanceiraUpdateRequestDTO dto, Financeiro financeiro) {
        MetaFinanceira meta = new MetaFinanceira();
        meta.setNome(dto.getNome());
        meta.setValorMeta(dto.getValorMeta());
        meta.setDataLimite(dto.getDataLimite());
        meta.setFinanceiro(financeiro);
        return meta;
    }
}
