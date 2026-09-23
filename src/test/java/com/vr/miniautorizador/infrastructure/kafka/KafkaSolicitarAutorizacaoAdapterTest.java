package com.vr.miniautorizador.infrastructure.kafka;

import com.vr.miniautorizador.domain.exception.AutorizacaoIndisponivelException;
import com.vr.miniautorizador.domain.model.SolicitacaoTransacao;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Cobre os caminhos em que a resposta da autorizacao nao chega: timeout e interrupcao da
 * thread que aguarda. O caminho feliz (resposta recebida) e coberto pelos testes de
 * aceitacao e E2E, que usam um Kafka de verdade.
 */
@ExtendWith(MockitoExtension.class)
class KafkaSolicitarAutorizacaoAdapterTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private final SolicitacaoTransacao solicitacao = new SolicitacaoTransacao("123", "1234", new BigDecimal("10.00"));

    @AfterEach
    void limparSinalDeInterrupcao() {
        // Garante que um teste nao deixe a thread do JUnit marcada como interrompida.
        Thread.interrupted();
    }

    @Test
    void deveLancarAutorizacaoIndisponivelQuandoARespostaNaoChegarDentroDoTimeout() {
        KafkaSolicitarAutorizacaoAdapter adapter = novoAdapter(50);

        assertThatThrownBy(() -> adapter.autorizar(solicitacao))
                .isInstanceOf(AutorizacaoIndisponivelException.class);
    }

    @Test
    void deveRestaurarOSinalDeInterrupcaoQuandoAThreadForInterrompidaAguardandoAResposta() {
        KafkaSolicitarAutorizacaoAdapter adapter = novoAdapter(5_000);
        Thread.currentThread().interrupt();

        assertThatThrownBy(() -> adapter.autorizar(solicitacao))
                .isInstanceOf(AutorizacaoIndisponivelException.class)
                .hasCauseInstanceOf(InterruptedException.class);
        assertThat(Thread.currentThread().isInterrupted()).isTrue();
    }

    private KafkaSolicitarAutorizacaoAdapter novoAdapter(long timeoutMs) {
        return new KafkaSolicitarAutorizacaoAdapter(
                kafkaTemplate, new KafkaTopicos("topico-teste"), timeoutMs);
    }
}
