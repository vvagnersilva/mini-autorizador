package com.vr.miniautorizador.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CartaoTest {

    @Test
    void deveCriarCartaoNovoComSaldoInicialInformado() {
        Cartao cartao = Cartao.novo("6549873025634501", "hash-da-senha", new BigDecimal("500.00"));

        assertThat(cartao.getNumeroCartao()).isEqualTo("6549873025634501");
        assertThat(cartao.getSenhaCodificada()).isEqualTo("hash-da-senha");
        assertThat(cartao.getSaldo()).isEqualByComparingTo("500.00");
    }

    @Test
    void devePossuirSaldoSuficienteQuandoValorForMenorQueOSaldo() {
        Cartao cartao = new Cartao("123", "hash", new BigDecimal("500.00"));

        assertThat(cartao.possuiSaldoSuficiente(new BigDecimal("499.99"))).isTrue();
    }

    @Test
    void devePossuirSaldoSuficienteQuandoValorForExatamenteIgualAoSaldo() {
        Cartao cartao = new Cartao("123", "hash", new BigDecimal("500.00"));

        assertThat(cartao.possuiSaldoSuficiente(new BigDecimal("500.00"))).isTrue();
    }

    @Test
    void naoDevePossuirSaldoSuficienteQuandoValorForMaiorQueOSaldo() {
        Cartao cartao = new Cartao("123", "hash", new BigDecimal("100.00"));

        assertThat(cartao.possuiSaldoSuficiente(new BigDecimal("100.01"))).isFalse();
    }

    @Test
    void doisCartoesComMesmoNumeroDevemSerIguaisIndependenteDoSaldoOuSenha() {
        Cartao a = new Cartao("123", "hash-1", new BigDecimal("500.00"));
        Cartao b = new Cartao("123", "hash-2", new BigDecimal("10.00"));

        assertThat(a)
                .isEqualTo(b)
                .hasSameHashCodeAs(b);
    }

    @Test
    void naoDevePermitirCriarCartaoComNumeroNulo() {
        assertThatThrownBy(() -> new Cartao(null, "hash", BigDecimal.TEN))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void naoDevePermitirCriarCartaoComSaldoNulo() {
        assertThatThrownBy(() -> new Cartao("123", "hash", null))
                .isInstanceOf(NullPointerException.class);
    }
}
