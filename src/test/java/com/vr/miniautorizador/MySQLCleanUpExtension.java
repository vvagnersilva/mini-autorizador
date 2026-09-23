package com.vr.miniautorizador;

import com.vr.miniautorizador.infrastructure.persistence.CartaoJpaRepository;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Garante que cada teste E2E comece com o banco vazio, ja que o contexto Spring
 * (e o MySQL) sao compartilhados entre os testes.
 */
public class MySQLCleanUpExtension implements BeforeEachCallback {

    @Override
    public void beforeEach(final ExtensionContext context) {
        final var appContext = SpringExtension.getApplicationContext(context);

        appContext.getBean(CartaoJpaRepository.class).deleteAllInBatch();
    }
}
