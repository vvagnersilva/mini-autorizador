package com.vr.miniautorizador.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Agregado raiz do dominio. Representa um cartao de beneficio (VR/VA).
 * Objeto imutavel: qualquer alteracao de saldo produz uma nova instancia.
 */
public final class Cartao {

    private final String numeroCartao;
    private final String senhaCodificada;
    private final BigDecimal saldo;

    public Cartao(String numeroCartao, String senhaCodificada, BigDecimal saldo) {
        this.numeroCartao = Objects.requireNonNull(numeroCartao, "numeroCartao e obrigatorio");
        this.senhaCodificada = Objects.requireNonNull(senhaCodificada, "senhaCodificada e obrigatoria");
        this.saldo = Objects.requireNonNull(saldo, "saldo e obrigatorio");
    }

    public static Cartao novo(String numeroCartao, String senhaCodificada, BigDecimal saldoInicial) {
        return new Cartao(numeroCartao, senhaCodificada, saldoInicial);
    }

    public boolean possuiSaldoSuficiente(BigDecimal valorTransacao) {
        return saldo.compareTo(valorTransacao) >= 0;
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

    @Override
    public boolean equals(Object o) {
        return o instanceof Cartao outro && numeroCartao.equals(outro.numeroCartao);
    }

    @Override
    public int hashCode() {
        return Objects.hash(numeroCartao);
    }

    @Override
    public String toString() {
        return "Cartao{numeroCartao='%s', saldo=%s}".formatted(numeroCartao, saldo);
    }
}
