package com.vr.miniautorizador.application.config;

import com.vr.miniautorizador.domain.port.CodificadorDeSenha;
import com.vr.miniautorizador.domain.regra.RegraAutorizacao;
import com.vr.miniautorizador.domain.regra.SaldoSuficienteRegra;
import com.vr.miniautorizador.domain.regra.SenhaCorretaRegra;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Composicao das regras de autorizacao (padrao Strategy) na ordem exigida pelo
 * enunciado: senha correta antes de saldo disponivel.
 * <p>
 * As classes de regra em si (pacote {@code domain.regra}) sao POJOs sem nenhuma
 * anotacao de framework - esta classe e o unico ponto onde o dominio e "amarrado"
 * ao container Spring, preservando a regra de dependencia da Clean Architecture
 * (o dominio nao conhece o framework; e o framework que conhece o dominio).
 */
@Configuration
public class RegrasAutorizacaoConfig {

    @Bean
    public List<RegraAutorizacao> regrasAutorizacao(CodificadorDeSenha codificadorDeSenha) {
        return List.of(
                new SenhaCorretaRegra(codificadorDeSenha),
                new SaldoSuficienteRegra());
    }
}
