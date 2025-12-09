package com.lifeboard.dto.transacao;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lifeboard.model.enums.CategoriaTransacao;
import com.lifeboard.model.enums.TipoTransacao;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransacaoRequestDTO {

    @NotNull(message = "Descrição é obrigatório.")
    @Size(min = 3, max = 150, message = "A descrição da transação deve ter entre 3 e 150 caracteres")
    private String descricao;

    @NotNull(message = "Valor é obrigatório.")
    @DecimalMin(value = "0.01", inclusive = true, message = "O valor da transação deve ser maior que zero.")
    private BigDecimal valor;

    @NotNull(message = "Tipo é obrigatório.")
    private TipoTransacao tipo;

    @NotNull(message = "Categoria é obrigatório.")
    private CategoriaTransacao categoria;

    @NotNull(message = "O ID de um Financeiro é obrigatório.")
    @JsonProperty("id_financeiro")
    private Long idFinanceiro;
}
