package com.vr.miniautorizador.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;

public record CriarCartaoRequest(
        @NotBlank(message = "numeroCartao e obrigatorio") String numeroCartao,
        @NotBlank(message = "senha e obrigatoria") String senha) {
}
