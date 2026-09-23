package com.vr.miniautorizador.application.port.in;

import com.vr.miniautorizador.domain.model.ResultadoAutorizacao;
import com.vr.miniautorizador.domain.model.SolicitacaoTransacao;

/**
 * Caso de uso (input port) exposto ao adaptador web: solicita a autorizacao de uma
 * transacao e aguarda (de forma sincrona, do ponto de vista do cliente HTTP) o resultado
 * processado de forma assincrona via Kafka.
 */
public interface SolicitarAutorizacaoTransacaoUseCase {

    ResultadoAutorizacao autorizar(SolicitacaoTransacao solicitacao);
}
