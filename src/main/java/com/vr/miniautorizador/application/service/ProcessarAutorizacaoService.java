package com.vr.miniautorizador.application.service;

import com.vr.miniautorizador.application.port.in.ProcessarAutorizacaoUseCase;
import com.vr.miniautorizador.domain.model.Cartao;
import com.vr.miniautorizador.domain.model.ResultadoAutorizacao;
import com.vr.miniautorizador.domain.model.SolicitacaoTransacao;
import com.vr.miniautorizador.domain.port.RepositorioCartao;
import com.vr.miniautorizador.domain.regra.RegraAutorizacao;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Motor de autorizacao: implementacao real das regras de negocio descritas no desafio.
 * <p>
 * Executado pelo consumidor Kafka (fora da requisicao HTTP), aplica em ordem as regras de
 * autorizacao registradas ({@link RegraAutorizacao}) e, caso todas sejam satisfeitas,
 * efetiva o debito atomico do saldo do cartao.
 * <p>
 * Nenhum comando {@code if} e utilizado nesta classe: a composicao e feita via
 * Optional/Stream e o padrao Strategy ({@link RegraAutorizacao}).
 */
@Service
public class ProcessarAutorizacaoService implements ProcessarAutorizacaoUseCase {

    private final RepositorioCartao repositorioCartao;
    private final List<RegraAutorizacao> regras;

    public ProcessarAutorizacaoService(RepositorioCartao repositorioCartao, List<RegraAutorizacao> regras) {
        this.repositorioCartao = repositorioCartao;
        this.regras = regras;
    }

    @Override
    public ResultadoAutorizacao processar(SolicitacaoTransacao solicitacao) {
        return repositorioCartao.buscarPorNumero(solicitacao.numeroCartao())
                .map(cartao -> aplicarRegras(cartao, solicitacao))
                .orElse(ResultadoAutorizacao.CARTAO_INEXISTENTE);
    }

    private ResultadoAutorizacao aplicarRegras(Cartao cartao, SolicitacaoTransacao solicitacao) {
        return regras.stream()
                .flatMap(regra -> regra.validar(cartao, solicitacao).stream())
                .findFirst()
                .orElseGet(() -> debitar(cartao.getNumeroCartao(), solicitacao.valor()));
    }

    private ResultadoAutorizacao debitar(String numeroCartao, BigDecimal valor) {
        int linhasAfetadas = repositorioCartao.debitarSeSaldoSuficiente(numeroCartao, valor);
        return linhasAfetadas == 1 ? ResultadoAutorizacao.APROVADA : ResultadoAutorizacao.SALDO_INSUFICIENTE;
    }
}
