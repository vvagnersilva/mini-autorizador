package com.vr.miniautorizador.infrastructure.security;

import com.vr.miniautorizador.domain.port.CodificadorDeSenha;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Adapter de {@link CodificadorDeSenha} usando o {@link PasswordEncoder} (BCrypt) do
 * Spring Security. A senha do cartao nunca e persistida em texto puro.
 */
@Component
public class BCryptCodificadorDeSenhaAdapter implements CodificadorDeSenha {

    private final PasswordEncoder passwordEncoder;

    public BCryptCodificadorDeSenhaAdapter(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String codificar(String senhaPura) {
        return passwordEncoder.encode(senhaPura);
    }

    @Override
    public boolean confere(String senhaPura, String senhaCodificada) {
        return passwordEncoder.matches(senhaPura, senhaCodificada);
    }
}
