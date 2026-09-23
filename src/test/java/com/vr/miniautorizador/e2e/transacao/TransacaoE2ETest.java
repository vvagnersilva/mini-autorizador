package com.vr.miniautorizador.e2e.transacao;

import com.vr.miniautorizador.E2ETest;
import com.vr.miniautorizador.e2e.MockDsl;
import com.vr.miniautorizador.infrastructure.persistence.CartaoJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@E2ETest
class TransacaoE2ETest implements MockDsl {

    private static final String NUMERO_CARTAO = "6549873025634501";
    private static final String SENHA = "1234";
    private static final BigDecimal CEM_REAIS = new BigDecimal("100.00");

    @Autowired
    private MockMvc mvc;

    @Autowired
    private CartaoJpaRepository cartaoRepository;

    @Override
    public MockMvc mvc() {
        return this.mvc;
    }

    @Test
    void comoMaquininhaDevoConseguirAutorizarUmaTransacaoEDebitarOSaldo() throws Exception {
        assertThat(cartaoRepository.count()).isZero();

        dadoUmCartao(NUMERO_CARTAO, SENHA);

        realizarTransacao(NUMERO_CARTAO, SENHA, CEM_REAIS)
                .andExpect(status().isCreated())
                .andExpect(content().string("OK"));

        assertThat(saldoNoBanco()).isEqualByComparingTo("400.00");
        assertThat(saldoDoCartao(NUMERO_CARTAO)).isEqualByComparingTo("400.00");
    }

    @Test
    void comoMaquininhaDevoRealizarTransacoesAteOSaldoFicarInsuficiente() throws Exception {
        assertThat(cartaoRepository.count()).isZero();

        dadoUmCartao(NUMERO_CARTAO, SENHA);

        for (int i = 1; i <= 5; i++) {
            realizarTransacao(NUMERO_CARTAO, SENHA, CEM_REAIS)
                    .andExpect(status().isCreated())
                    .andExpect(content().string("OK"));
        }

        realizarTransacao(NUMERO_CARTAO, SENHA, CEM_REAIS)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string("SALDO_INSUFICIENTE"));

        assertThat(saldoNoBanco()).isEqualByComparingTo("0.00");
    }

    @Test
    void comoMaquininhaDevoTerATransacaoRecusadaComSenhaInvalida() throws Exception {
        assertThat(cartaoRepository.count()).isZero();

        dadoUmCartao(NUMERO_CARTAO, SENHA);

        realizarTransacao(NUMERO_CARTAO, "0000", CEM_REAIS)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string("SENHA_INVALIDA"));

        assertThat(saldoNoBanco()).isEqualByComparingTo("500.00");
    }

    @Test
    void comoMaquininhaDevoTerATransacaoRecusadaComCartaoInexistente() throws Exception {
        assertThat(cartaoRepository.count()).isZero();

        realizarTransacao("0000000000000000", SENHA, CEM_REAIS)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string("CARTAO_INEXISTENTE"));
    }

    @Test
    void comoMaquininhaDevoReceber400AoEnviarTransacaoComValorInvalido() throws Exception {
        assertThat(cartaoRepository.count()).isZero();

        dadoUmCartao(NUMERO_CARTAO, SENHA);

        realizarTransacao(NUMERO_CARTAO, SENHA, BigDecimal.ZERO)
                .andExpect(status().isBadRequest())
                .andExpect(content().string("REQUISICAO_INVALIDA"));

        assertThat(saldoNoBanco()).isEqualByComparingTo("500.00");
    }

    /**
     * Mesmo cenario do ConcorrenciaTransacaoTest, mas contra MySQL real: e aqui que o
     * debito atomico (UPDATE condicional) precisa segurar a corrida entre transacoes.
     */
    @Test
    void transacoesSimultaneasNoMesmoCartaoNaoDevemDeixarOSaldoNegativo() throws Exception {
        assertThat(cartaoRepository.count()).isZero();

        dadoUmCartao(NUMERO_CARTAO, SENHA);

        final var quantidadeDeRequisicoes = 10;
        final var tarefas = IntStream.range(0, quantidadeDeRequisicoes)
                .<Callable<Integer>>mapToObj(i -> () -> realizarTransacao(NUMERO_CARTAO, SENHA, CEM_REAIS)
                        .andReturn().getResponse().getStatus())
                .toList();

        final List<Integer> statusRecebidos;
        try (final var executor = Executors.newFixedThreadPool(quantidadeDeRequisicoes)) {
            statusRecebidos = executor.invokeAll(tarefas).stream().map(TransacaoE2ETest::resultado).toList();
        }

        assertThat(statusRecebidos).filteredOn(s -> s == 201).hasSize(5);
        assertThat(statusRecebidos).filteredOn(s -> s == 422).hasSize(5);
        assertThat(saldoNoBanco()).isEqualByComparingTo("0.00");
    }

    private BigDecimal saldoNoBanco() {
        return cartaoRepository.findById(NUMERO_CARTAO).orElseThrow().getSaldo();
    }

    private static Integer resultado(final Future<Integer> futuro) {
        try {
            return futuro.get();
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
