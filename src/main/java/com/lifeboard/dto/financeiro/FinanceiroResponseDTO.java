package com.lifeboard.dto.financeiro;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.lifeboard.dto.meta.MetaFinanceiraResponseDTO;
import com.lifeboard.dto.transacao.TransacaoResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder({ "id_financeiro", "saldo_atual", "salario_mensal", "id_usuario", "transacoes", "metas"})
public class FinanceiroResponseDTO {

    @JsonProperty("id_financeiro")
    private Long id;
    @JsonProperty("saldo_atual")
    private BigDecimal saldoAtual;
    @JsonProperty("salario_mensal")
    private BigDecimal salarioMensal;
    @JsonProperty("id_usuario")
    private Long usuarioId;
    private List<TransacaoResponseDTO> transacoes;
    private List<MetaFinanceiraResponseDTO> metas;
}
