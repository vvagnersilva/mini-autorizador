package com.vr.miniautorizador.application.service;

import com.vr.miniautorizador.application.dto.CartaoCriado;
import com.vr.miniautorizador.application.dto.CriarCartaoComando;
import com.vr.miniautorizador.application.port.in.CriarCartaoUseCase;
import com.vr.miniautorizador.domain.exception.CartaoJaExisteException;
import com.vr.miniautorizador.domain.model.Cartao;
import com.vr.miniautorizador.domain.port.CodificadorDeSenha;
import com.vr.miniautorizador.domain.port.RepositorioCartao;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Implementacao do caso de uso de criacao de cartao.
 * Cada cartao novo comeca com o saldo inicial configurado (padrao: R$ 500,00).
 */
@Service
public class CriarCartaoService implements CriarCartaoUseCase {

    private final RepositorioCartao repositorioCartao;
    private final CodificadorDeSenha codificadorDeSenha;
    private final BigDecimal saldoInicial;

    public CriarCartaoService(RepositorioCartao repositorioCartao,
                               CodificadorDeSenha codificadorDeSenha,
                               @org.springframework.beans.factory.annotation.Value("${app.cartao.saldo-inicial}") BigDecimal saldoInicial) {
        this.repositorioCartao = repositorioCartao;
        this.codificadorDeSenha = codificadorDeSenha;
        this.saldoInicial = saldoInicial;
    }

    @Override
    public CartaoCriado criar(CriarCartaoComando comando) {
        return repositorioCartao.buscarPorNumero(comando.numeroCartao())
                .<CartaoCriado>map(cartaoExistente -> {
                    throw new CartaoJaExisteException(comando.numeroCartao(), comando.senha());
                })
                .orElseGet(() -> criarNovoCartao(comando));
    }

    private CartaoCriado criarNovoCartao(CriarCartaoComando comando) {
        String senhaCodificada = codificadorDeSenha.codificar(comando.senha());
        Cartao cartao = Cartao.novo(comando.numeroCartao(), senhaCodificada, saldoInicial);
        repositorioCartao.salvar(cartao);
        return new CartaoCriado(cartao.getNumeroCartao(), comando.senha());
    }
}
