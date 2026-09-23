package com.vr.miniautorizador.domain.model;

import java.math.BigDecimal;

/**
 * Representa a intencao de debito solicitada por uma maquininha de cartao.
 *
 * @param numeroCartao numero do cartao usado como meio de pagamento
 * @param senha        senha informada em texto puro (comparada contra o hash armazenado)
 * @param valor        valor da transacao a ser debitado do saldo, caso autorizada
 */
public record SolicitacaoTransacao(String numeroCartao, String senha, BigDecimal valor) {
}
