package com.vr.miniautorizador.domain.regra;

import com.vr.miniautorizador.domain.model.Cartao;
import com.vr.miniautorizador.domain.model.ResultadoAutorizacao;
import com.vr.miniautorizador.domain.model.SolicitacaoTransacao;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class SaldoSuficienteRegraTest {

    private final SaldoSuficienteRegra regra = new SaldoSuficienteRegra();

    @Test
    void deveDevolverVazioQuandoSaldoForSuficiente() {
        Cartao cartao = new Cartao("123", "hash", new BigDecimal("500.00"));
        SolicitacaoTransacao solicitacao = new SolicitacaoTransacao("123", "1234", new BigDecimal("100.00"));

        assertThat(regra.validar(cartao, solicitacao)).isEmpty();
    }

    @Test
    void deveDevolverSaldoInsuficienteQuandoValorForMaiorQueSaldo() {
        Cartao cartao = new Cartao("123", "hash", new BigDecimal("50.00"));
        SolicitacaoTransacao solicitacao = new SolicitacaoTransacao("123", "1234", new BigDecimal("100.00"));

        assertThat(regra.validar(cartao, solicitacao)).contains(ResultadoAutorizacao.SALDO_INSUFICIENTE);
    }
}
