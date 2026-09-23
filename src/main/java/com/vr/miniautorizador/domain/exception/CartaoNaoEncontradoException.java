package com.vr.miniautorizador.domain.exception;

/**
 * Lancada ao consultar um cartao que nao existe na base.
 */
public class CartaoNaoEncontradoException extends RuntimeException {

    public CartaoNaoEncontradoException(String numeroCartao) {
        super("Cartao nao encontrado: " + numeroCartao);
    }
}
