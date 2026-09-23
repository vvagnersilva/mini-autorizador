package com.vr.miniautorizador.e2e;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vr.miniautorizador.infrastructure.web.dto.CriarCartaoRequest;
import com.vr.miniautorizador.infrastructure.web.dto.TransacaoRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.math.BigDecimal;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * DSL dos testes E2E: esconde os detalhes de MockMvc (JSON, autenticacao, headers)
 * e deixa os testes escritos na linguagem do negocio (criar cartao, consultar saldo,
 * realizar transacao).
 * <p>
 * Todas as chamadas passam pelo filtro de seguranca real com as credenciais da
 * maquininha (HTTP Basic), como um cliente de verdade faria.
 */
public interface MockDsl {

    RequestPostProcessor MAQUININHA = httpBasic("username", "password");

    ObjectMapper JSON = new ObjectMapper();

    MockMvc mvc();

    /**
     * Cartao
     */

    default void dadoUmCartao(final String numeroCartao, final String senha) throws Exception {
        criarCartao(numeroCartao, senha).andExpect(status().isCreated());
    }

    default ResultActions criarCartao(final String numeroCartao, final String senha) throws Exception {
        return this.post("/cartoes", new CriarCartaoRequest(numeroCartao, senha));
    }

    default ResultActions consultarSaldo(final String numeroCartao) throws Exception {
        final var aRequest = get("/cartoes/{numeroCartao}", numeroCartao)
                .with(MAQUININHA)
                .accept(MediaType.APPLICATION_JSON);

        return this.mvc().perform(aRequest);
    }

    default BigDecimal saldoDoCartao(final String numeroCartao) throws Exception {
        final var json = consultarSaldo(numeroCartao)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse().getContentAsString();

        return new BigDecimal(json);
    }

    /**
     * Transacao
     */

    default ResultActions realizarTransacao(final String numeroCartao, final String senha, final BigDecimal valor) throws Exception {
        return this.post("/transacoes", new TransacaoRequest(numeroCartao, senha, valor));
    }

    private ResultActions post(final String url, final Object body) throws Exception {
        final var aRequest = MockMvcRequestBuilders.post(url)
                .with(MAQUININHA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(body));

        return this.mvc().perform(aRequest);
    }

    private static String toJson(final Object body) {
        try {
            return JSON.writeValueAsString(body);
        } catch (final JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }
}
