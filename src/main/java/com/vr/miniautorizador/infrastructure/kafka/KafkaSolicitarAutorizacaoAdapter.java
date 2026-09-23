package com.vr.miniautorizador.infrastructure.kafka;

import com.vr.miniautorizador.application.port.in.SolicitarAutorizacaoTransacaoUseCase;
import com.vr.miniautorizador.domain.exception.AutorizacaoIndisponivelException;
import com.vr.miniautorizador.domain.model.ResultadoAutorizacao;
import com.vr.miniautorizador.domain.model.SolicitacaoTransacao;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Adapter que implementa o caso de uso {@link SolicitarAutorizacaoTransacaoUseCase}
 * publicando a solicitacao no Kafka e aguardando (de forma sincrona, do ponto de
 * vista do controller que fez a chamada) a resposta correlacionada por um id unico.
 * <p>
 * A resposta e recebida no topico de resposta exclusivo desta instancia
 * ({@link KafkaTopicos#getResultadoAutorizacao()}), o que evita que a resposta de
 * uma requisicao atendida por esta instancia seja consumida por outra instancia da
 * aplicacao.
 */
@Component
public class KafkaSolicitarAutorizacaoAdapter implements SolicitarAutorizacaoTransacaoUseCase {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaTopicos topicos;
    private final long timeoutMs;
    private final ConcurrentMap<String, CompletableFuture<ResultadoAutorizacao>> respostasPendentes =
            new ConcurrentHashMap<>();

    public KafkaSolicitarAutorizacaoAdapter(KafkaTemplate<String, Object> kafkaTemplate,
                                             KafkaTopicos topicos,
                                             @Value("${app.kafka.timeout-resposta-ms}") long timeoutMs) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicos = topicos;
        this.timeoutMs = timeoutMs;
    }

    @Override
    public ResultadoAutorizacao autorizar(SolicitacaoTransacao solicitacao) {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<ResultadoAutorizacao> futuro = new CompletableFuture<>();
        respostasPendentes.put(correlationId, futuro);

        kafkaTemplate.send(
                topicos.getSolicitacaoAutorizacao(),
                solicitacao.numeroCartao(),
                new SolicitacaoTransacaoMensagem(
                        correlationId,
                        solicitacao.numeroCartao(),
                        solicitacao.senha(),
                        solicitacao.valor(),
                        topicos.getResultadoAutorizacao()));

        return aguardarResposta(correlationId, futuro);
    }

    private ResultadoAutorizacao aguardarResposta(String correlationId, CompletableFuture<ResultadoAutorizacao> futuro) {
        try {
            return futuro.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            // Restaura o sinal de interrupcao para que quem chamou saiba que a thread foi interrompida.
            Thread.currentThread().interrupt();
            throw new AutorizacaoIndisponivelException(correlationId, e);
        } catch (TimeoutException | ExecutionException e) {
            throw new AutorizacaoIndisponivelException(correlationId, e);
        } finally {
            respostasPendentes.remove(correlationId);
        }
    }

    @KafkaListener(
            topics = "#{kafkaTopicos.resultadoAutorizacao}",
            groupId = "#{kafkaTopicos.resultadoAutorizacao}",
            containerFactory = "kafkaListenerContainerFactory")
    public void receberResultado(ResultadoTransacaoMensagem mensagem) {
        Optional.ofNullable(respostasPendentes.get(mensagem.correlationId()))
                .ifPresent(futuro -> futuro.complete(ResultadoAutorizacao.valueOf(mensagem.resultado())));
    }
}
