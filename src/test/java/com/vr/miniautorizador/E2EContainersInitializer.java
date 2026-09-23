package com.vr.miniautorizador;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.lifecycle.Startables;

/**
 * Sobe (uma unica vez por JVM) os containers de MySQL e Kafka usados pelos testes
 * {@link E2ETest} e aponta o contexto Spring para eles.
 * <p>
 * Os containers sao compartilhados entre todas as classes E2E (padrao "singleton
 * container"), evitando subir um MySQL/Kafka novo por classe. O isolamento entre
 * testes fica a cargo do {@link MySQLCleanUpExtension}. As imagens sao as mesmas do
 * docker-compose.yml, para que o teste rode contra a mesma infraestrutura do ambiente local.
 */
public class E2EContainersInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final MySQLContainer<?> MYSQL_CONTAINER = new MySQLContainer<>("mysql:5.7")
            .withDatabaseName("miniautorizador")
            .withUsername("miniautorizador")
            .withPassword("miniautorizador");

    // Sem atraso no rebalance inicial do grupo: o consumidor fica pronto logo na subida do contexto.
    private static final KafkaContainer KAFKA_CONTAINER = new KafkaContainer("apache/kafka:3.7.0")
            .withEnv("KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS", "0");

    static {
        Startables.deepStart(MYSQL_CONTAINER, KAFKA_CONTAINER).join();
    }

    @Override
    public void initialize(final ConfigurableApplicationContext context) {
        TestPropertyValues.of(
                "spring.datasource.url=" + MYSQL_CONTAINER.getJdbcUrl(),
                "spring.datasource.username=" + MYSQL_CONTAINER.getUsername(),
                "spring.datasource.password=" + MYSQL_CONTAINER.getPassword(),
                "spring.kafka.bootstrap-servers=" + KAFKA_CONTAINER.getBootstrapServers()
        ).applyTo(context.getEnvironment());
    }
}
