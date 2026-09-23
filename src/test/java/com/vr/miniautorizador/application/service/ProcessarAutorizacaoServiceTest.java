package com.vr.miniautorizador.application.service;

import com.vr.miniautorizador.domain.model.Cartao;
import com.vr.miniautorizador.domain.model.ResultadoAutorizacao;
import com.vr.miniautorizador.domain.model.SolicitacaoTransacao;
import com.vr.miniautorizador.domain.port.RepositorioCartao;
import com.vr.miniautorizador.domain.regra.RegraAutorizacao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessarAutorizacaoServiceTest {

    @Mock
    private RepositorioCartao repositorioCartao;

    @Mock
    private RegraAutorizacao primeiraRegra;

    @Mock
    private RegraAutorizacao segundaRegra;

    private final SolicitacaoTransacao solicitacao = new SolicitacaoTransacao("123", "1234", new BigDecimal("10.00"));

    @Test
    void deveDevolverCartaoInexistenteQuandoCartaoNaoForEncontrado_semAvaliarRegras() {
        when(repositorioCartao.buscarPorNumero("123")).thenReturn(Optional.empty());
        ProcessarAutorizacaoService service = new ProcessarAutorizacaoService(
                repositorioCartao, List.of(primeiraRegra, segundaRegra));

        ResultadoAutorizacao resultado = service.processar(solicitacao);

        assertThat(resultado).isEqualTo(ResultadoAutorizacao.CARTAO_INEXISTENTE);
        verify(primeiraRegra, never()).validar(any(), any());
        verify(segundaRegra, never()).validar(any(), any());
    }

    @Test
    void deveDevolverRecusaDaPrimeiraRegraQueFalhar_semAvaliarAsSeguintes() {
        Cartao cartao = new Cartao("123", "hash", new BigDecimal("500.00"));
        when(repositorioCartao.buscarPorNumero("123")).thenReturn(Optional.of(cartao));
        when(primeiraRegra.validar(cartao, solicitacao)).thenReturn(Optional.of(ResultadoAutorizacao.SENHA_INVALIDA));
        ProcessarAutorizacaoService service = new ProcessarAutorizacaoService(
                repositorioCartao, List.of(primeiraRegra, segundaRegra));

        ResultadoAutorizacao resultado = service.processar(solicitacao);

        assertThat(resultado).isEqualTo(ResultadoAutorizacao.SENHA_INVALIDA);
        verify(segundaRegra, never()).validar(any(), any());
        verify(repositorioCartao, never()).debitarSeSaldoSuficiente(any(), any());
    }

    @Test
    void deveDebitarEDevolverAprovadaQuandoTodasAsRegrasPassarem() {
        Cartao cartao = new Cartao("123", "hash", new BigDecimal("500.00"));
        when(repositorioCartao.buscarPorNumero("123")).thenReturn(Optional.of(cartao));
        when(primeiraRegra.validar(cartao, solicitacao)).thenReturn(Optional.empty());
        when(segundaRegra.validar(cartao, solicitacao)).thenReturn(Optional.empty());
        when(repositorioCartao.debitarSeSaldoSuficiente("123", new BigDecimal("10.00"))).thenReturn(1);
        ProcessarAutorizacaoService service = new ProcessarAutorizacaoService(
                repositorioCartao, List.of(primeiraRegra, segundaRegra));

        ResultadoAutorizacao resultado = service.processar(solicitacao);

        assertThat(resultado).isEqualTo(ResultadoAutorizacao.APROVADA);
        verify(repositorioCartao, times(1)).debitarSeSaldoSuficiente(eq("123"), eq(new BigDecimal("10.00")));
    }

    @Test
    void deveDevolverSaldoInsuficienteQuandoDebitoAtomicoNaoAfetarNenhumaLinha() {
        Cartao cartao = new Cartao("123", "hash", new BigDecimal("500.00"));
        when(repositorioCartao.buscarPorNumero("123")).thenReturn(Optional.of(cartao));
        when(primeiraRegra.validar(cartao, solicitacao)).thenReturn(Optional.empty());
        when(segundaRegra.validar(cartao, solicitacao)).thenReturn(Optional.empty());
        // simula corrida: outra transacao concorrente zerou o saldo entre a leitura e o debito
        when(repositorioCartao.debitarSeSaldoSuficiente("123", new BigDecimal("10.00"))).thenReturn(0);
        ProcessarAutorizacaoService service = new ProcessarAutorizacaoService(
                repositorioCartao, List.of(primeiraRegra, segundaRegra));

        ResultadoAutorizacao resultado = service.processar(solicitacao);

        assertThat(resultado).isEqualTo(ResultadoAutorizacao.SALDO_INSUFICIENTE);
    }
}
