package com.vr.miniautorizador;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Meta-anotacao dos testes ponta a ponta (E2E).
 * <p>
 * Sobe o contexto Spring completo contra infraestrutura REAL (MySQL + Kafka via
 * Testcontainers), exercitando o fluxo HTTP -> seguranca -> caso de uso -> Kafka ->
 * regras de dominio -> MySQL -> Kafka -> HTTP. O banco e limpo antes de cada teste.
 * <p>
 * Sem Docker/Podman disponivel, as classes anotadas sao desabilitadas (nao falham).
 * Execucao isolada: {@code mvn test -Dgroups=e2eTest}.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@ActiveProfiles("test-e2e")
@SpringBootTest
@ContextConfiguration(initializers = E2EContainersInitializer.class)
@ExtendWith(MySQLCleanUpExtension.class)
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
@Tag("e2eTest")
public @interface E2ETest {
}
