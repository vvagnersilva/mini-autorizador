package com.vr.miniautorizador.application.service;

import com.vr.miniautorizador.domain.exception.CartaoNaoEncontradoException;
import com.vr.miniautorizador.domain.model.Cartao;
import com.vr.miniautorizador.domain.port.RepositorioCartao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultarSaldoServiceTest {

    @Mock
    private RepositorioCartao repositorioCartao;

    @Test
    void deveDevolverSaldoDoCartaoExistente() {
        ConsultarSaldoService service = new ConsultarSaldoService(repositorioCartao);
        when(repositorioCartao.buscarPorNumero("123")).thenReturn(
                Optional.of(new Cartao("123", "hash", new BigDecimal("495.15"))));

        BigDecimal saldo = service.consultarSaldo("123");

        assertThat(saldo).isEqualByComparingTo("495.15");
    }

    @Test
    void deveLancarExcecaoQuandoCartaoNaoExiste() {
        ConsultarSaldoService service = new ConsultarSaldoService(repositorioCartao);
        when(repositorioCartao.buscarPorNumero("999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.consultarSaldo("999"))
                .isInstanceOf(CartaoNaoEncontradoException.class);
    }
}
