package com.vr.miniautorizador.infrastructure.persistence;

import com.vr.miniautorizador.domain.model.Cartao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

/**
 * Testes de integracao reais contra um banco H2 (compativel com o schema usado em
 * producao), validando a traducao domino <-> JPA e, principalmente, a atomicidade
 * do debito condicional sob concorrencia real (multiplas threads).
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
@Import(RepositorioCartaoJpaAdapter.class)
class RepositorioCartaoJpaAdapterTest {

    @Autowired
    private RepositorioCartaoJpaAdapter repositorio;

    @Test
    void deveSalvarEBuscarCartaoPorNumero() {
        repositorio.salvar(new Cartao("111", "hash-111", new BigDecimal("500.00")));

        var encontrado = repositorio.buscarPorNumero("111");

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getNumeroCartao()).isEqualTo("111");
        assertThat(encontrado.get().getSenhaCodificada()).isEqualTo("hash-111");
        assertThat(encontrado.get().getSaldo()).isEqualByComparingTo("500.00");
    }

    @Test
    void devolverVazioQuandoCartaoNaoExiste() {
        assertThat(repositorio.buscarPorNumero("nao-existe")).isEmpty();
    }

    @Test
    void existePorNumeroDeveRefletirPersistencia() {
        assertThat(repositorio.existePorNumero("222")).isFalse();

        repositorio.salvar(new Cartao("222", "hash", new BigDecimal("500.00")));

        assertThat(repositorio.existePorNumero("222")).isTrue();
    }

    @Test
    void deveDebitarQuandoSaldoForSuficiente() {
        repositorio.salvar(new Cartao("333", "hash", new BigDecimal("500.00")));

        int linhasAfetadas = repositorio.debitarSeSaldoSuficiente("333", new BigDecimal("100.00"));

        assertThat(linhasAfetadas).isEqualTo(1);
        assertThat(repositorio.buscarPorNumero("333").get().getSaldo()).isEqualByComparingTo("400.00");
    }

    @Test
    void naoDeveDebitarQuandoSaldoForInsuficiente_eSaldoDevePermanecerInalterado() {
        repositorio.salvar(new Cartao("444", "hash", new BigDecimal("50.00")));

        int linhasAfetadas = repositorio.debitarSeSaldoSuficiente("444", new BigDecimal("100.00"));

        assertThat(linhasAfetadas).isEqualTo(0);
        assertThat(repositorio.buscarPorNumero("444").get().getSaldo()).isEqualByComparingTo("50.00");
    }

    @Test
    void naoDeveDebitarQuandoCartaoNaoExiste() {
        assertThat(repositorio.debitarSeSaldoSuficiente("nao-existe", BigDecimal.TEN)).isEqualTo(0);
    }

    @Test
    // @DataJpaTest envolve cada teste numa unica transacao com rollback automatico;
    // aqui isso e indesejado, pois as threads concorrentes abrem suas PROPRIAS transacoes
    // e nao enxergariam o cartao inserido pela thread principal caso a transacao dela
    // ainda estivesse aberta (nao commitada). Desligamos esse wrapping so para este teste.
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void debitoConcorrenteNuncaDeveDeixarSaldoNegativo() throws InterruptedException {
        repositorio.salvar(new Cartao("555", "hash", new BigDecimal("500.00")));
        int quantidadeThreads = 10;
        BigDecimal valorPorTransacao = new BigDecimal("100.00");
        ExecutorService executor = Executors.newFixedThreadPool(quantidadeThreads);
        CountDownLatch partidaSincronizada = new CountDownLatch(1);
        CountDownLatch todasTerminaram = new CountDownLatch(quantidadeThreads);
        AtomicInteger aprovadas = new AtomicInteger();

        for (int i = 0; i < quantidadeThreads; i++) {
            executor.submit(() -> {
                try {
                    partidaSincronizada.await();
                    int linhas = repositorio.debitarSeSaldoSuficiente("555", valorPorTransacao);
                    aprovadas.addAndGet(linhas);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    todasTerminaram.countDown();
                }
            });
        }

        partidaSincronizada.countDown();
        todasTerminaram.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // saldo de 500 comporta exatamente 5 transacoes de 100 concorrentes
        assertThat(aprovadas.get()).isEqualTo(5);
        assertThat(repositorio.buscarPorNumero("555").get().getSaldo()).isEqualByComparingTo("0.00");
    }

    @Test
    void deveTraduzirEntidadeJpaParaAgregadoDeDominioCorretamente() {
        repositorio.salvar(new Cartao("666", "hash-especifico", new BigDecimal("123.45")));

        Cartao cartao = repositorio.buscarPorNumero("666").orElseThrow();

        assertThat(cartao).isInstanceOf(Cartao.class);
        assertThat(List.of(cartao.getNumeroCartao(), cartao.getSenhaCodificada()))
                .containsExactly("666", "hash-especifico");
    }
}
