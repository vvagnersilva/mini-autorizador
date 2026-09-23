package com.vr.miniautorizador.infrastructure.web.dto;

/**
 * Corpo de resposta da criacao de cartao, respeitando o contrato REST informado
 * (campo "senha" antes de "numeroCartao").
 */
public record CartaoResponse(String senha, String numeroCartao) {
}
