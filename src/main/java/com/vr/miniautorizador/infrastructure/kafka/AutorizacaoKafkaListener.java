package com.vr.miniautorizador.infrastructure.kafka;

import com.vr.miniautorizador.application.port.in.ProcessarAutorizacaoUseCase;
import com.vr.miniautorizador.domain.model.ResultadoAutorizacao;
import com.vr.miniautorizador.domain.model.SolicitacaoTransacao;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Consumidor Kafka que efetivamente processa a autorizacao (fora da requisicao HTTP).
 * Aplica as regras de negocio via {@link ProcessarAutorizacaoUseCase} e publica o
 * resultado no topico de resposta indicado na propria mensagem recebida.
 */
@Component
public class AutorizacaoKafkaListener {

    private final ProcessarAutorizacaoUseCase processarAutorizacaoUseCase;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public AutorizacaoKafkaListener(ProcessarAutorizacaoUseCase processarAutorizacaoUseCase,
                                     KafkaTemplate<String, Object> kafkaTemplate) {
        this.processarAutorizacaoUseCase = processarAutorizacaoUseCase;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(
            topics = "${app.kafka.topico-solicitacao-autorizacao}",
            groupId = "${app.kafka.grupo-consumidor}",
            containerFactory = "kafkaListenerContainerFactory")
    public void processar(SolicitacaoTransacaoMensagem mensagem) {
        ResultadoAutorizacao resultado = processarAutorizacaoUseCase.processar(
                new SolicitacaoTransacao(mensagem.numeroCartao(), mensagem.senha(), mensagem.valor()));

        kafkaTemplate.send(mensagem.topicoResposta(), mensagem.correlationId(),
                new ResultadoTransacaoMensagem(mensagem.correlationId(), resultado.name()));
    }
}
