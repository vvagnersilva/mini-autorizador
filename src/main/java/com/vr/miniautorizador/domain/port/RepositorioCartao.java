package com.vr.miniautorizador.domain.port;

import com.vr.miniautorizador.domain.model.Cartao;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Porta de saida (driven port) para persistencia do agregado {@link Cartao}.
 * Implementada na camada de infraestrutura (adapter JPA).
 */
public interface RepositorioCartao {

    boolean existePorNumero(String numeroCartao);

    Optional<Cartao> buscarPorNumero(String numeroCartao);

    void salvar(Cartao cartao);

    /**
     * Debita atomicamente o valor informado do saldo do cartao, mas apenas se o saldo
     * atual for suficiente. A verificacao e o debito ocorrem em uma unica instrucao SQL
     * condicional (compare-and-set no banco), eliminando condicoes de corrida entre
     * transacoes concorrentes (inclusive entre instancias diferentes da aplicacao).
     *
     * @return quantidade de linhas afetadas: {@code 1} se o debito foi realizado,
     *         {@code 0} se o saldo era insuficiente (ou o cartao deixou de existir).
     */
    int debitarSeSaldoSuficiente(String numeroCartao, BigDecimal valor);
}
