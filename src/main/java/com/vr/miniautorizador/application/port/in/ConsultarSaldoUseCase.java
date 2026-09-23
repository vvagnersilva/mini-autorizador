package com.vr.miniautorizador.application.port.in;

import java.math.BigDecimal;

/**
 * Caso de uso (input port): consulta do saldo disponivel de um cartao.
 */
public interface ConsultarSaldoUseCase {

    BigDecimal consultarSaldo(String numeroCartao);
}
