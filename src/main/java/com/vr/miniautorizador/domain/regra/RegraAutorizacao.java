package com.vr.miniautorizador.domain.regra;

import com.vr.miniautorizador.domain.model.Cartao;
import com.vr.miniautorizador.domain.model.ResultadoAutorizacao;
import com.vr.miniautorizador.domain.model.SolicitacaoTransacao;

import java.util.Optional;

/**
 * Estrategia (padrao Strategy) que representa uma regra de autorizacao de transacao.
 * <p>
 * Cada regra recebe o cartao ja localizado e a solicitacao de transacao e devolve:
 * <ul>
 *     <li>{@link Optional#empty()} caso a regra seja satisfeita (transacao pode seguir); ou</li>
 *     <li>um {@link ResultadoAutorizacao} de recusa, caso a regra tenha barrado a transacao.</li>
 * </ul>
 * A composicao de varias regras (ver {@code ProcessarAutorizacaoService}) elimina a necessidade
 * de blocos {@code if/else} explicitos no fluxo de decisao: percorre-se a lista de regras via
 * stream e a primeira recusa encontrada (se houver) determina o resultado final.
 */
public interface RegraAutorizacao {

    Optional<ResultadoAutorizacao> validar(Cartao cartao, SolicitacaoTransacao solicitacao);
}
