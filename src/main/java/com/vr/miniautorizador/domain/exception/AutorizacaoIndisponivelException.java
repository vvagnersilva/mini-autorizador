package com.vr.miniautorizador.domain.exception;

/**
 * Lancada quando o processamento assincrono (via Kafka) da autorizacao nao responde
 * dentro do tempo limite configurado.
 */
public class AutorizacaoIndisponivelException extends RuntimeException {

    public AutorizacaoIndisponivelException(String correlationId, Throwable causa) {
        super("Nao foi possivel obter resultado da autorizacao (correlationId=" + correlationId + ")", causa);
    }
}
