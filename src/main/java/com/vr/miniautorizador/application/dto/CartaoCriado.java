package com.vr.miniautorizador.application.dto;

/**
 * Resultado da criacao de um cartao. A senha e devolvida em texto puro (o mesmo valor
 * recebido na requisicao) para atender o contrato REST - o valor nunca e lido do hash
 * persistido.
 */
public record CartaoCriado(String numeroCartao, String senha) {
}
