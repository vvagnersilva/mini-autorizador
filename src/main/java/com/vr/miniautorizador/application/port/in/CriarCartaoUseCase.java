package com.vr.miniautorizador.application.port.in;

import com.vr.miniautorizador.application.dto.CartaoCriado;
import com.vr.miniautorizador.application.dto.CriarCartaoComando;

/**
 * Caso de uso (input port): criacao de um novo cartao de beneficio.
 */
public interface CriarCartaoUseCase {

    CartaoCriado criar(CriarCartaoComando comando);
}
