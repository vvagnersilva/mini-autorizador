package com.vr.miniautorizador.infrastructure.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransacaoRequest(
        @NotBlank(message = "numeroCartao e obrigatorio") String numeroCartao,
        @NotBlank(message = "senhaCartao e obrigatoria") String senhaCartao,
        @NotNull(message = "valor e obrigatorio")
        @DecimalMin(value = "0.01", message = "valor deve ser maior que zero") BigDecimal valor) {
}
