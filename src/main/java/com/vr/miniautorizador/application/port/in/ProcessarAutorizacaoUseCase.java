package com.vr.miniautorizador.application.port.in;

import com.vr.miniautorizador.domain.model.ResultadoAutorizacao;
import com.vr.miniautorizador.domain.model.SolicitacaoTransacao;

/**
 * Caso de uso (input port) invocado pelo consumidor Kafka: aplica de fato as regras de
 * autorizacao e, se aprovado, debita o saldo do cartao.
 */
public interface ProcessarAutorizacaoUseCase {

    ResultadoAutorizacao processar(SolicitacaoTransacao solicitacao);
}
