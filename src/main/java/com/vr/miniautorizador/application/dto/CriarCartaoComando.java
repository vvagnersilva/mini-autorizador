package com.vr.miniautorizador.application.dto;

/**
 * Comando de entrada para criacao de cartao.
 */
public record CriarCartaoComando(String numeroCartao, String senha) {
}
