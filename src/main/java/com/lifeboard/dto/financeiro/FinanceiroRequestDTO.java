package com.lifeboard.dto.financeiro;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FinanceiroRequestDTO {

    @NotNull(message = "Saldo Atual é obrigatório.")
    @DecimalMin(value = "0.0", inclusive = true, message = "O saldo deve ser igual ou maior que zero.")
    @JsonProperty("saldo_atual")
    private BigDecimal saldoAtual;

    @NotNull(message = "Salário é obrigatório.")
    @DecimalMin(value = "0.0", inclusive = true, message = "O salário deve ser maior que zero.")
    @JsonProperty("salario_mensal")
    private BigDecimal salarioMensal;

    @NotNull(message = "O ID de um Usuário é obrigatório.")
    @JsonProperty("id_usuario")
    private Long usuarioId;
}
