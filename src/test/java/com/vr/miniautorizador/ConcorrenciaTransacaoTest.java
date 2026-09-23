package com.vr.miniautorizador;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Reproduz o cenario de concorrencia citado no desafio: varias transacoes disparadas
 * "ao mesmo tempo" para o mesmo cartao. Sobe o contexto completo (HTTP real + Kafka
 * embarcado + H2) e dispara requisicoes HTTP verdadeiras em paralelo (via threads
 * independentes), simulando multiplas maquininhas/instancias batendo no mesmo cartao.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 3, brokerProperties = {"auto.create.topics.enable=true"})
class ConcorrenciaTransacaoTest {

    private static final String SENHA = "1234";

    @Autowired
    private TestRestTemplate restTemplate;

    private String numeroCartao;

    @BeforeEach
    void criarCartaoComSaldoPadrao() {
        numeroCartao = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        ResponseEntity<Map> resposta = autenticado().postForEntity(
                "/cartoes", Map.of("numeroCartao", numeroCartao, "senha", SENHA), Map.class);
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    private TestRestTemplate autenticado() {
        return restTemplate.withBasicAuth("username", "password");
    }

    @Test
    void dezTransacoesSimultaneasDeCemReaisEmCartaoComQuinhentosDevemAprovarApenasCinco() throws Exception {
        int quantidadeDeRequisicoes = 10;
        BigDecimal valorPorTransacao = new BigDecimal("100.00");
        ExecutorService executor = Executors.newFixedThreadPool(quantidadeDeRequisicoes);

        List<Callable<HttpStatusCode>> tarefas = IntStream.range(0, quantidadeDeRequisicoes)
                .<Callable<HttpStatusCode>>mapToObj(i -> () -> autenticado().postForEntity(
                                "/transacoes",
                                Map.of("numeroCartao", numeroCartao, "senhaCartao", SENHA, "valor", valorPorTransacao),
                                String.class)
                        .getStatusCode())
                .toList();

        List<Future<HttpStatusCode>> futuros = executor.invokeAll(tarefas);
        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);

        List<HttpStatusCode> resultados = futuros.stream().map(this::resultadoOuFalha).collect(Collectors.toList());

        long aprovadas = resultados.stream().filter(HttpStatus.CREATED::equals).count();
        long recusadas = resultados.stream().filter(HttpStatus.UNPROCESSABLE_ENTITY::equals).count();

        assertThat(aprovadas).isEqualTo(5);
        assertThat(recusadas).isEqualTo(5);

        ResponseEntity<BigDecimal> saldoFinal = autenticado().getForEntity("/cartoes/" + numeroCartao, BigDecimal.class);
        assertThat(saldoFinal.getBody()).isEqualByComparingTo("0.00");
    }

    private HttpStatusCode resultadoOuFalha(Future<HttpStatusCode> futuro) {
        try {
            return futuro.get();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
