package com.vr.miniautorizador;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Teste de aceitacao ponta a ponta, seguindo EXATAMENTE o roteiro descrito no
 * enunciado do desafio, na mesma ordem: criar cartao -> checar saldo -> repetir
 * transacoes ate saldo insuficiente -> senha invalida -> cartao inexistente.
 * <p>
 * Sobe o contexto Spring completo (web + seguranca + JPA/H2 + Kafka embarcado),
 * validando o fluxo real (HTTP -> Kafka -> regras de dominio -> banco -> Kafka -> HTTP).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, brokerProperties = {"auto.create.topics.enable=true"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MiniAutorizadorAcceptanceTest {

    private static final String NUMERO_CARTAO = "6549873025634501";
    private static final String SENHA = "1234";

    @Autowired
    private TestRestTemplate restTemplate;

    private TestRestTemplate autenticado() {
        return restTemplate.withBasicAuth("user", "password");
    }

    @Test
    @Order(1)
    void deveCriarUmCartaoComSucesso() {
        ResponseEntity<Map> resposta = autenticado().postForEntity(
                "/cartoes", Map.of("numeroCartao", NUMERO_CARTAO, "senha", SENHA), Map.class);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resposta.getBody()).containsEntry("numeroCartao", NUMERO_CARTAO).containsEntry("senha", SENHA);
    }

    @Test
    @Order(2)
    void naoDeveCriarCartaoDuplicado() {
        ResponseEntity<Map> resposta = autenticado().postForEntity(
                "/cartoes", Map.of("numeroCartao", NUMERO_CARTAO, "senha", SENHA), Map.class);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    @Order(3)
    void deveConsultarOSaldoDoCartaoRecemCriado() {
        ResponseEntity<BigDecimal> resposta = autenticado().getForEntity(
                "/cartoes/" + NUMERO_CARTAO, BigDecimal.class);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resposta.getBody()).isEqualByComparingTo("500.00");
    }

    @Test
    @Order(4)
    void deveRealizarTransacoesAteInformarSaldoInsuficiente() {
        // saldo inicial 500,00 / transacoes de 100,00 -> 5 aprovadas, a 6a deve ser recusada
        for (int i = 1; i <= 5; i++) {
            ResponseEntity<String> transacao = autenticado().postForEntity(
                    "/transacoes", Map.of("numeroCartao", NUMERO_CARTAO, "senhaCartao", SENHA, "valor", new BigDecimal("100.00")), String.class);

            assertThat(transacao.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(transacao.getBody()).isEqualTo("OK");

            BigDecimal saldoEsperado = new BigDecimal("500.00").subtract(new BigDecimal("100.00").multiply(BigDecimal.valueOf(i)));
            ResponseEntity<BigDecimal> saldo = autenticado().getForEntity("/cartoes/" + NUMERO_CARTAO, BigDecimal.class);
            assertThat(saldo.getBody()).isEqualByComparingTo(saldoEsperado);
        }

        ResponseEntity<String> transacaoRecusada = autenticado().postForEntity(
                "/transacoes", Map.of("numeroCartao", NUMERO_CARTAO, "senhaCartao", SENHA, "valor", new BigDecimal("100.00")), String.class);

        assertThat(transacaoRecusada.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(transacaoRecusada.getBody()).isEqualTo("SALDO_INSUFICIENTE");
    }

    @Test
    @Order(5)
    void deveRecusarTransacaoComSenhaInvalida() {
        ResponseEntity<String> resposta = autenticado().postForEntity(
                "/transacoes", Map.of("numeroCartao", NUMERO_CARTAO, "senhaCartao", "0000", "valor", new BigDecimal("1.00")), String.class);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(resposta.getBody()).isEqualTo("SENHA_INVALIDA");
    }

    @Test
    @Order(6)
    void deveRecusarTransacaoComCartaoInexistente() {
        ResponseEntity<String> resposta = autenticado().postForEntity(
                "/transacoes", Map.of("numeroCartao", "0000000000000000", "senhaCartao", SENHA, "valor", new BigDecimal("1.00")), String.class);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(resposta.getBody()).isEqualTo("CARTAO_INEXISTENTE");
    }

    @Test
    @Order(7)
    void deveRetornar404AoConsultarSaldoDeCartaoInexistente() {
        ResponseEntity<String> resposta = autenticado().getForEntity("/cartoes/0000000000000000", String.class);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Order(8)
    void deveRetornar401SemAutenticacao() {
        ResponseEntity<String> resposta = restTemplate.getForEntity("/cartoes/" + NUMERO_CARTAO, String.class);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
