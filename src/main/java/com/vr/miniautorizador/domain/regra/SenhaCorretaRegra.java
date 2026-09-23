package com.vr.miniautorizador.domain.regra;

import com.vr.miniautorizador.domain.model.Cartao;
import com.vr.miniautorizador.domain.model.ResultadoAutorizacao;
import com.vr.miniautorizador.domain.model.SolicitacaoTransacao;
import com.vr.miniautorizador.domain.port.CodificadorDeSenha;

import java.util.Optional;

/**
 * Regra: a senha informada na transacao deve corresponder a senha cadastrada no cartao.
 */
public class SenhaCorretaRegra implements RegraAutorizacao {

    private final CodificadorDeSenha codificadorDeSenha;

    public SenhaCorretaRegra(CodificadorDeSenha codificadorDeSenha) {
        this.codificadorDeSenha = codificadorDeSenha;
    }

    @Override
    public Optional<ResultadoAutorizacao> validar(Cartao cartao, SolicitacaoTransacao solicitacao) {
        boolean senhaConfere = codificadorDeSenha.confere(solicitacao.senha(), cartao.getSenhaCodificada());
        return senhaConfere ? Optional.empty() : Optional.of(ResultadoAutorizacao.SENHA_INVALIDA);
    }
}
