package com.vr.miniautorizador.infrastructure.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.Map;

/**
 * Infraestrutura Kafka: fabricas de producer/consumer com serializacao JSON e o
 * topico fixo de solicitacao de autorizacao (criado automaticamente pelo {@link KafkaAdmin}).
 * <p>
 * O topico de resposta (dinamico, por instancia - ver {@link KafkaTopicos}) nao possui
 * um {@link NewTopic} declarado aqui; ele e criado sob demanda pelo proprio broker
 * (auto criacao de topicos), ja que seu nome so existe em tempo de execucao.
 */
@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic topicoSolicitacaoAutorizacao(KafkaTopicos topicos) {
        return TopicBuilder.name(topicos.getSolicitacaoAutorizacao())
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> configs = kafkaProperties.buildProducerProperties(null);
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configs);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public ConsumerFactory<String, Object> consumerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> configs = kafkaProperties.buildConsumerProperties(null);
        configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(configs);
    }

    /**
     * Declarada explicitamente (em vez de deixar a auto-configuracao do Spring Boot
     * inferir): devido a apagamento/covariancia de generics, o mecanismo de seleção
     * automatica de {@code ConsumerFactory<Object, Object>} do Boot nao reconhece um
     * bean {@code ConsumerFactory<String, Object>} customizado, e silenciosamente cria
     * a sua propria fabrica (com deserializadores padrao). Ao nomear e tipar o bean
     * exatamente como esperado por {@code @KafkaListener(containerFactory = "...")},
     * eliminamos essa ambiguidade.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
            ConsumerFactory<String, Object> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }
}
