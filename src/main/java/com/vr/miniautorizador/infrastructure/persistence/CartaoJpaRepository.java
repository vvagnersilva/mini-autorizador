package com.vr.miniautorizador.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

/**
 * Repositorio Spring Data responsavel pelo acesso a tabela "cartao".
 */
public interface CartaoJpaRepository extends JpaRepository<CartaoJpaEntity, String> {

    /**
     * Debito atomico: uma unica instrucao SQL condicional (compare-and-set) que so
     * atualiza a linha se o saldo atual for suficiente. O banco de dados serializa
     * escritas concorrentes na mesma linha, eliminando corridas entre transacoes
     * simultaneas (inclusive vindas de instancias diferentes da aplicacao).
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update CartaoJpaEntity c set c.saldo = c.saldo - :valor "
            + "where c.numeroCartao = :numeroCartao and c.saldo >= :valor")
    int debitarSeSaldoSuficiente(@Param("numeroCartao") String numeroCartao, @Param("valor") BigDecimal valor);
}
