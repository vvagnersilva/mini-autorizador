package com.vr.miniautorizador.domain.model;

/**
 * Resultado possivel de uma tentativa de autorizacao de transacao.
 * Os nomes correspondem exatamente ao contrato REST exposto (motivo de recusa).
 */
public enum ResultadoAutorizacao {

    APROVADA,
    SALDO_INSUFICIENTE,
    SENHA_INVALIDA,
    CARTAO_INEXISTENTE;

    public boolean isAprovada() {
        return this == APROVADA;
    }
}
