package com.vr.miniautorizador.infrastructure.kafka;

import java.math.BigDecimal;

/**
 * Mensagem publicada no topico de solicitacao de autorizacao. Carrega o topico de
 * resposta (unico por instancia da aplicacao) para que o processador saiba para onde
 * enviar o resultado.
 */
public record SolicitacaoTransacaoMensagem(
        String correlationId,
        String numeroCartao,
        String senha,
        BigDecimal valor,
        String topicoResposta) {
}
