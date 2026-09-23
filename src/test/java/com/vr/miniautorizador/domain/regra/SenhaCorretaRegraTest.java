package com.vr.miniautorizador.domain.regra;

import com.vr.miniautorizador.domain.model.Cartao;
import com.vr.miniautorizador.domain.model.ResultadoAutorizacao;
import com.vr.miniautorizador.domain.model.SolicitacaoTransacao;
import com.vr.miniautorizador.domain.port.CodificadorDeSenha;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SenhaCorretaRegraTest {

    @Mock
    private CodificadorDeSenha codificadorDeSenha;

    private SenhaCorretaRegra regra;

    @BeforeEach
    void setUp() {
        regra = new SenhaCorretaRegra(codificadorDeSenha);
    }

    @Test
    void deveDevolverVazioQuandoSenhaConferir() {
        Cartao cartao = new Cartao("123", "hash-armazenado", new BigDecimal("500.00"));
        SolicitacaoTransacao solicitacao = new SolicitacaoTransacao("123", "1234", BigDecimal.TEN);
        when(codificadorDeSenha.confere("1234", "hash-armazenado")).thenReturn(true);

        assertThat(regra.validar(cartao, solicitacao)).isEmpty();
    }

    @Test
    void deveDevolverSenhaInvalidaQuandoSenhaNaoConferir() {
        Cartao cartao = new Cartao("123", "hash-armazenado", new BigDecimal("500.00"));
        SolicitacaoTransacao solicitacao = new SolicitacaoTransacao("123", "0000", BigDecimal.TEN);
        when(codificadorDeSenha.confere("0000", "hash-armazenado")).thenReturn(false);

        assertThat(regra.validar(cartao, solicitacao)).contains(ResultadoAutorizacao.SENHA_INVALIDA);
    }
}
