package com.vr.miniautorizador.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * Entidade JPA que mapeia o cartao na tabela relacional (MySQL).
 * Mantida separada do agregado de dominio {@code Cartao} para nao acoplar
 * o modelo de negocio a anotacoes de persistencia (Clean Architecture).
 */
@Entity
@Table(name = "cartao")
public class CartaoJpaEntity {

    @Id
    @Column(name = "numero_cartao", length = 19, nullable = false)
    private String numeroCartao;

    @Column(name = "senha_codificada", length = 100, nullable = false)
    private String senhaCodificada;

    @Column(name = "saldo", precision = 15, scale = 2, nullable = false)
    private BigDecimal saldo;

    protected CartaoJpaEntity() {
        // exigido pelo JPA
    }

    public CartaoJpaEntity(String numeroCartao, String senhaCodificada, BigDecimal saldo) {
        this.numeroCartao = numeroCartao;
        this.senhaCodificada = senhaCodificada;
        this.saldo = saldo;
    }

    public String getNumeroCartao() {
        return numeroCartao;
    }

    public String getSenhaCodificada() {
        return senhaCodificada;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }
}
