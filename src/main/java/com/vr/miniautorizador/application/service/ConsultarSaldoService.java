package com.vr.miniautorizador.application.service;

import com.vr.miniautorizador.application.port.in.ConsultarSaldoUseCase;
import com.vr.miniautorizador.domain.exception.CartaoNaoEncontradoException;
import com.vr.miniautorizador.domain.model.Cartao;
import com.vr.miniautorizador.domain.port.RepositorioCartao;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Implementacao do caso de uso de consulta de saldo.
 */
@Service
public class ConsultarSaldoService implements ConsultarSaldoUseCase {

    private final RepositorioCartao repositorioCartao;

    public ConsultarSaldoService(RepositorioCartao repositorioCartao) {
        this.repositorioCartao = repositorioCartao;
    }

    @Override
    public BigDecimal consultarSaldo(String numeroCartao) {
        return repositorioCartao.buscarPorNumero(numeroCartao)
                .map(Cartao::getSaldo)
                .orElseThrow(() -> new CartaoNaoEncontradoException(numeroCartao));
    }
}
