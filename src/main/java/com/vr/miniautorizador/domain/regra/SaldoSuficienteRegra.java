package com.vr.miniautorizador.domain.regra;

import com.vr.miniautorizador.domain.model.Cartao;
import com.vr.miniautorizador.domain.model.ResultadoAutorizacao;
import com.vr.miniautorizador.domain.model.SolicitacaoTransacao;

import java.util.Optional;

/**
 * Regra: o cartao deve possuir saldo disponivel maior ou igual ao valor da transacao.
 * <p>
 * Esta e apenas uma verificacao "otimista" de leitura; a garantia definitiva contra
 * condicoes de corrida acontece no debito atomico executado no banco de dados
 * (ver {@code RepositorioCartaoJpaAdapter#debitarSeSaldoSuficiente}).
 */
public class SaldoSuficienteRegra implements RegraAutorizacao {

    @Override
    public Optional<ResultadoAutorizacao> validar(Cartao cartao, SolicitacaoTransacao solicitacao) {
        return cartao.possuiSaldoSuficiente(solicitacao.valor())
                ? Optional.empty()
                : Optional.of(ResultadoAutorizacao.SALDO_INSUFICIENTE);
    }
}
