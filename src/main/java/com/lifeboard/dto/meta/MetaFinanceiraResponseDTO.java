package com.lifeboard.dto.meta;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.lifeboard.model.enums.StatusMeta;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder({ "id_meta", "nome", "valor_meta", "valor_atual", "data_limite", "status", "id_financeiro"})
public class MetaFinanceiraResponseDTO {

    @JsonProperty("id_meta")
    private Long id;

    private String nome;

    @JsonProperty("valor_meta")
    private BigDecimal valorMeta;

    @JsonProperty("valor_atual")
    private BigDecimal valorAtual;

    @JsonProperty("data_limite")
    private LocalDate dataLimite;

    private StatusMeta status;

    @JsonProperty("id_financeiro")
    private Long idFinanceiro;
}
