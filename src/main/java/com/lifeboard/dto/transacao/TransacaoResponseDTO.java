package com.lifeboard.dto.transacao;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.lifeboard.model.enums.CategoriaTransacao;
import com.lifeboard.model.enums.TipoTransacao;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder({ "id_transacao", "descricao", "valor", "tipo", "data", "categoria", "id_financeiro"})
public class TransacaoResponseDTO {

    @JsonProperty("id_transacao")
    private Long id;
    private String descricao;
    private BigDecimal valor;
    private TipoTransacao tipo;
    private LocalDateTime data;
    private CategoriaTransacao categoria;
    @JsonProperty("id_financeiro")
    private Long idFinanceiro;
}
