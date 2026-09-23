package com.vr.miniautorizador.application.service;

import com.vr.miniautorizador.application.dto.CartaoCriado;
import com.vr.miniautorizador.application.dto.CriarCartaoComando;
import com.vr.miniautorizador.domain.exception.CartaoJaExisteException;
import com.vr.miniautorizador.domain.model.Cartao;
import com.vr.miniautorizador.domain.port.CodificadorDeSenha;
import com.vr.miniautorizador.domain.port.RepositorioCartao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CriarCartaoServiceTest {

    @Mock
    private RepositorioCartao repositorioCartao;

    @Mock
    private CodificadorDeSenha codificadorDeSenha;

    private CriarCartaoService service;

    @BeforeEach
    void setUp() {
        service = new CriarCartaoService(repositorioCartao, codificadorDeSenha, new BigDecimal("500.00"));
    }

    @Test
    void deveCriarCartaoComSaldoInicialESenhaCodificada() {
        CriarCartaoComando comando = new CriarCartaoComando("6549873025634501", "1234");
        when(repositorioCartao.buscarPorNumero("6549873025634501")).thenReturn(Optional.empty());
        when(codificadorDeSenha.codificar("1234")).thenReturn("hash-1234");

        CartaoCriado resultado = service.criar(comando);

        assertThat(resultado.numeroCartao()).isEqualTo("6549873025634501");
        assertThat(resultado.senha()).isEqualTo("1234");

        ArgumentCaptor<Cartao> captor = ArgumentCaptor.forClass(Cartao.class);
        verify(repositorioCartao).salvar(captor.capture());
        Cartao cartaoSalvo = captor.getValue();
        assertThat(cartaoSalvo.getNumeroCartao()).isEqualTo("6549873025634501");
        assertThat(cartaoSalvo.getSenhaCodificada()).isEqualTo("hash-1234");
        assertThat(cartaoSalvo.getSaldo()).isEqualByComparingTo("500.00");
    }

    @Test
    void naoDeveArmazenarSenhaEmTextoPuro() {
        when(repositorioCartao.buscarPorNumero(any())).thenReturn(Optional.empty());
        when(codificadorDeSenha.codificar("1234")).thenReturn("$2a$hash-bem-diferente");

        service.criar(new CriarCartaoComando("111", "1234"));

        ArgumentCaptor<Cartao> captor = ArgumentCaptor.forClass(Cartao.class);
        verify(repositorioCartao).salvar(captor.capture());
        assertThat(captor.getValue().getSenhaCodificada()).isNotEqualTo("1234");
    }

    @Test
    void deveLancarExcecaoQuandoCartaoJaExiste() {
        Cartao existente = new Cartao("6549873025634501", "hash-existente", new BigDecimal("500.00"));
        when(repositorioCartao.buscarPorNumero("6549873025634501")).thenReturn(Optional.of(existente));

        CriarCartaoComando comando = new CriarCartaoComando("6549873025634501", "1234");

        assertThatThrownBy(() -> service.criar(comando))
                .isInstanceOf(CartaoJaExisteException.class);

        verify(repositorioCartao, never()).salvar(any());
    }

    @Test
    void excecaoDeCartaoExistenteDeveCarregarNumeroESenhaOriginaisDaRequisicao() {
        when(repositorioCartao.buscarPorNumero("111")).thenReturn(Optional.of(
                new Cartao("111", "hash-diferente-do-informado", BigDecimal.ZERO)));

        CriarCartaoComando comando = new CriarCartaoComando("111", "senha-informada");

        assertThatThrownBy(() -> service.criar(comando))
                .isInstanceOf(CartaoJaExisteException.class)
                .satisfies(ex -> {
                    CartaoJaExisteException cartaoJaExisteException = (CartaoJaExisteException) ex;
                    assertThat(cartaoJaExisteException.getNumeroCartao()).isEqualTo("111");
                    assertThat(cartaoJaExisteException.getSenha()).isEqualTo("senha-informada");
                });
    }
}
