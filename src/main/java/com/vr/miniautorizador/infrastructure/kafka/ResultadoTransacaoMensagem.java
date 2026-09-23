package com.vr.miniautorizador.infrastructure.kafka;

/**
 * Mensagem de resposta publicada pelo processador da autorizacao, contendo o
 * resultado ({@link com.vr.miniautorizador.domain.model.ResultadoAutorizacao#name()}).
 */
public record ResultadoTransacaoMensagem(String correlationId, String resultado) {
}
