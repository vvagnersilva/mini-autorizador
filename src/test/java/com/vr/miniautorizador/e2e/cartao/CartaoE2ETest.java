package com.vr.miniautorizador.e2e.cartao;

import com.vr.miniautorizador.E2ETest;
import com.vr.miniautorizador.e2e.MockDsl;
import com.vr.miniautorizador.infrastructure.persistence.CartaoJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@E2ETest
class CartaoE2ETest implements MockDsl {

    private static final String NUMERO_CARTAO = "6549873025634501";
    private static final String SENHA = "1234";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private CartaoJpaRepository cartaoRepository;

    @Override
    public MockMvc mvc() {
        return this.mvc;
    }

    @Test
    void comoMaquininhaDevoConseguirCriarUmCartaoComSaldoInicial() throws Exception {
        assertThat(cartaoRepository.count()).isZero();

        criarCartao(NUMERO_CARTAO, SENHA)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numeroCartao", equalTo(NUMERO_CARTAO)))
                .andExpect(jsonPath("$.senha", equalTo(SENHA)));

        final var cartaoPersistido = cartaoRepository.findById(NUMERO_CARTAO).orElseThrow();

        assertThat(cartaoPersistido.getSaldo()).isEqualByComparingTo("500.00");
        assertThat(cartaoPersistido.getSenhaCodificada())
                .as("a senha nunca deve ser gravada em texto puro")
                .isNotEqualTo(SENHA)
                .startsWith("$2");
    }

    @Test
    void comoMaquininhaNaoDevoConseguirCriarUmCartaoDuplicado() throws Exception {
        assertThat(cartaoRepository.count()).isZero();

        dadoUmCartao(NUMERO_CARTAO, SENHA);

        criarCartao(NUMERO_CARTAO, SENHA)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.numeroCartao", equalTo(NUMERO_CARTAO)))
                .andExpect(jsonPath("$.senha", equalTo(SENHA)));

        assertThat(cartaoRepository.count()).isEqualTo(1);
    }

    @Test
    void comoMaquininhaDevoConseguirConsultarOSaldoDeUmCartao() throws Exception {
        assertThat(cartaoRepository.count()).isZero();

        dadoUmCartao(NUMERO_CARTAO, SENHA);

        assertThat(saldoDoCartao(NUMERO_CARTAO)).isEqualByComparingTo("500.00");
    }

    @Test
    void comoMaquininhaDevoReceber404AoConsultarSaldoDeCartaoInexistente() throws Exception {
        assertThat(cartaoRepository.count()).isZero();

        consultarSaldo("0000000000000000")
                .andExpect(status().isNotFound());
    }

    @Test
    void comoMaquininhaDevoReceber400AoCriarCartaoSemSenha() throws Exception {
        assertThat(cartaoRepository.count()).isZero();

        criarCartao(NUMERO_CARTAO, "")
                .andExpect(status().isBadRequest())
                .andExpect(content().string("REQUISICAO_INVALIDA"));

        assertThat(cartaoRepository.count()).isZero();
    }

    @Test
    void semAutenticacaoDevoReceber401() throws Exception {
        mvc.perform(get("/cartoes/{numeroCartao}", NUMERO_CARTAO))
                .andExpect(status().isUnauthorized());
    }
}
