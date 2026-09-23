package com.vr.miniautorizador.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class BCryptCodificadorDeSenhaAdapterTest {

    private BCryptCodificadorDeSenhaAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new BCryptCodificadorDeSenhaAdapter(new BCryptPasswordEncoder());
    }

    @Test
    void deveCodificarSenhaGerandoHashDiferenteDoTextoOriginal() {
        String hash = adapter.codificar("1234");

        assertThat(hash).isNotEqualTo("1234");
        assertThat(hash).startsWith("$2a$");
    }

    @Test
    void deveConferirSenhaCorretaContraOHash() {
        String hash = adapter.codificar("1234");

        assertThat(adapter.confere("1234", hash)).isTrue();
    }

    @Test
    void naoDeveConferirSenhaIncorretaContraOHash() {
        String hash = adapter.codificar("1234");

        assertThat(adapter.confere("0000", hash)).isFalse();
    }

    @Test
    void duasCodificacoesDaMesmaSenhaDevemGerarHashesDiferentes() {
        String hash1 = adapter.codificar("1234");
        String hash2 = adapter.codificar("1234");

        assertThat(hash1).isNotEqualTo(hash2);
        assertThat(adapter.confere("1234", hash1)).isTrue();
        assertThat(adapter.confere("1234", hash2)).isTrue();
    }
}
