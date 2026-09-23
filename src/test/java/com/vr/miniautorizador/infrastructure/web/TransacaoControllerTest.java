package com.vr.miniautorizador.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vr.miniautorizador.application.port.in.SolicitarAutorizacaoTransacaoUseCase;
import com.vr.miniautorizador.domain.model.ResultadoAutorizacao;
import com.vr.miniautorizador.infrastructure.security.SecurityConfig;
import com.vr.miniautorizador.infrastructure.web.dto.TransacaoRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransacaoController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@TestPropertySource(properties = {"app.seguranca.usuario=username", "app.seguranca.senha=password"})
class TransacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SolicitarAutorizacaoTransacaoUseCase solicitarAutorizacaoTransacaoUseCase;

    @Test
    void deveRetornar201ComCorpoOkQuandoAprovada() throws Exception {
        when(solicitarAutorizacaoTransacaoUseCase.autorizar(any())).thenReturn(ResultadoAutorizacao.APROVADA);

        mockMvc.perform(post("/transacoes")
                        .with(httpBasic("username", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new TransacaoRequest("6549873025634501", "1234", new BigDecimal("10.00")))))
                .andExpect(status().isCreated())
                .andExpect(content().string("OK"));
    }

    @Test
    void deveRetornar422ComSaldoInsuficiente() throws Exception {
        when(solicitarAutorizacaoTransacaoUseCase.autorizar(any())).thenReturn(ResultadoAutorizacao.SALDO_INSUFICIENTE);

        mockMvc.perform(post("/transacoes")
                        .with(httpBasic("username", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new TransacaoRequest("6549873025634501", "1234", new BigDecimal("10000.00")))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string("SALDO_INSUFICIENTE"));
    }

    @Test
    void deveRetornar422ComSenhaInvalida() throws Exception {
        when(solicitarAutorizacaoTransacaoUseCase.autorizar(any())).thenReturn(ResultadoAutorizacao.SENHA_INVALIDA);

        mockMvc.perform(post("/transacoes")
                        .with(httpBasic("username", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new TransacaoRequest("6549873025634501", "0000", new BigDecimal("10.00")))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string("SENHA_INVALIDA"));
    }

    @Test
    void deveRetornar422ComCartaoInexistente() throws Exception {
        when(solicitarAutorizacaoTransacaoUseCase.autorizar(any())).thenReturn(ResultadoAutorizacao.CARTAO_INEXISTENTE);

        mockMvc.perform(post("/transacoes")
                        .with(httpBasic("username", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new TransacaoRequest("0000000000000000", "1234", new BigDecimal("10.00")))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string("CARTAO_INEXISTENTE"));
    }

    @Test
    void deveRetornar401QuandoNaoAutenticado() throws Exception {
        mockMvc.perform(post("/transacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new TransacaoRequest("123", "1234", BigDecimal.TEN))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornar400QuandoValorForNegativo() throws Exception {
        mockMvc.perform(post("/transacoes")
                        .with(httpBasic("username", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new TransacaoRequest("123", "1234", new BigDecimal("-10.00")))))
                .andExpect(status().isBadRequest());
    }
}
