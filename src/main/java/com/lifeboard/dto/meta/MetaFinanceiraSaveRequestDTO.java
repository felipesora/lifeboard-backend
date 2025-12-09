package com.lifeboard.dto.meta;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class MetaFinanceiraSaveRequestDTO {

    @NotNull(message = "Nome da Meta é obrigatório.")
    @Size(min = 3, max = 150, message = "O nome da meta deve ter entre 3 e 100 caracteres")
    private String nome;

    @NotNull(message = "O valor da meta é obrigatório.")
    @JsonProperty("valor_meta")
    private BigDecimal valorMeta;

    @NotNull(message = "O valor atual é obrigatório.")
    @JsonProperty("valor_atual")
    private BigDecimal valorAtual;

    @NotNull(message = "A data limite da meta é obrigatório.")
    @JsonProperty("data_limite")
    private LocalDate dataLimite;

    @NotNull(message = "O ID de um Financeiro é obrigatório.")
    @JsonProperty("id_financeiro")
    private Long idFinanceiro;
}
