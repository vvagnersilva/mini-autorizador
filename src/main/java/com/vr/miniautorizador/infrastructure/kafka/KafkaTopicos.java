package com.vr.miniautorizador.infrastructure.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Fonte unica de verdade para os nomes dos topicos Kafka usados pelo fluxo de
 * autorizacao de transacoes.
 * <p>
 * O topico de solicitacao e fixo (configuravel), compartilhado por todas as
 * instancias da aplicacao (consumido em grupo, balanceado por particao).
 * <p>
 * O topico de resposta e <b>unico por instancia</b> (sufixo aleatorio gerado uma
 * unica vez na construcao deste bean singleton). Isso garante que a resposta de uma
 * solicitacao feita por esta instancia seja entregue exclusivamente a ela mesma,
 * mesmo quando ha varias instancias da aplicacao rodando simultaneamente atras do
 * mesmo cluster Kafka - eliminando o problema classico de "quem espera a resposta
 * pode nao ser quem a recebe" em um esquema ingenuo de topico de resposta compartilhado.
 */
@Component
public class KafkaTopicos {

    private final String solicitacaoAutorizacao;
    private final String resultadoAutorizacao;

    public KafkaTopicos(@Value("${app.kafka.topico-solicitacao-autorizacao}") String solicitacaoAutorizacao) {
        this.solicitacaoAutorizacao = solicitacaoAutorizacao;
        this.resultadoAutorizacao = solicitacaoAutorizacao + ".resultado." + UUID.randomUUID();
    }

    public String getSolicitacaoAutorizacao() {
        return solicitacaoAutorizacao;
    }

    public String getResultadoAutorizacao() {
        return resultadoAutorizacao;
    }
}
