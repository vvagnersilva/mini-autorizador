package com.vr.miniautorizador.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vr.miniautorizador.application.dto.CartaoCriado;
import com.vr.miniautorizador.application.port.in.ConsultarSaldoUseCase;
import com.vr.miniautorizador.application.port.in.CriarCartaoUseCase;
import com.vr.miniautorizador.domain.exception.CartaoJaExisteException;
import com.vr.miniautorizador.domain.exception.CartaoNaoEncontradoException;
import com.vr.miniautorizador.infrastructure.security.SecurityConfig;
import com.vr.miniautorizador.infrastructure.web.dto.CriarCartaoRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de contrato HTTP (com Spring Security real) do endpoint /cartoes,
 * cobrindo os 3 status possiveis definidos no enunciado: 201, 422 e 401.
 */
@WebMvcTest(CartaoController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@TestPropertySource(properties = {"app.seguranca.usuario=username", "app.seguranca.senha=password"})
class CartaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CriarCartaoUseCase criarCartaoUseCase;

    @MockBean
    private ConsultarSaldoUseCase consultarSaldoUseCase;

    @Test
    void deveRetornar201AoCriarCartaoComSucesso() throws Exception {
        when(criarCartaoUseCase.criar(any())).thenReturn(new CartaoCriado("6549873025634501", "1234"));

        mockMvc.perform(post("/cartoes")
                        .with(httpBasic("username", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CriarCartaoRequest("6549873025634501", "1234"))))
                .andExpect(status().isCreated())
                .andExpect(content().json("{\"senha\":\"1234\",\"numeroCartao\":\"6549873025634501\"}"));
    }

    @Test
    void deveRetornar422QuandoCartaoJaExiste() throws Exception {
        when(criarCartaoUseCase.criar(any())).thenThrow(new CartaoJaExisteException("6549873025634501", "1234"));

        mockMvc.perform(post("/cartoes")
                        .with(httpBasic("username", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CriarCartaoRequest("6549873025634501", "1234"))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().json("{\"senha\":\"1234\",\"numeroCartao\":\"6549873025634501\"}"));
    }

    @Test
    void deveRetornar401QuandoNaoAutenticado() throws Exception {
        mockMvc.perform(post("/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CriarCartaoRequest("123", "1234"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornar401QuandoCredenciaisInvalidas() throws Exception {
        mockMvc.perform(post("/cartoes")
                        .with(httpBasic("username", "senha-errada"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CriarCartaoRequest("123", "1234"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornar200ComSaldoDoCartao() throws Exception {
        when(consultarSaldoUseCase.consultarSaldo("6549873025634501")).thenReturn(new BigDecimal("495.15"));

        mockMvc.perform(get("/cartoes/6549873025634501").with(httpBasic("username", "password")))
                .andExpect(status().isOk())
                .andExpect(content().string("495.15"));
    }

    @Test
    void deveRetornar404QuandoCartaoNaoExiste() throws Exception {
        when(consultarSaldoUseCase.consultarSaldo("nao-existe"))
                .thenThrow(new CartaoNaoEncontradoException("nao-existe"));

        mockMvc.perform(get("/cartoes/nao-existe").with(httpBasic("username", "password")))
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));
    }
}
