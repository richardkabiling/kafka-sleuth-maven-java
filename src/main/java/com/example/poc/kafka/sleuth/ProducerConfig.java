package com.example.poc.kafka.sleuth;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.kafka.common.serialization.StringSerializer;;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

import static org.apache.kafka.clients.producer.ProducerConfig.*;
import static org.springframework.kafka.support.serializer.JsonSerializer.ADD_TYPE_INFO_HEADERS;

@Configuration
public class ProducerConfig {

    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(ADD_TYPE_INFO_HEADERS, "false");

        return props;
    }

    @Bean
    public JsonSerializer<Resource> valueSerializer() {
        return new JsonSerializer<>(new TypeReference<>() {
        });
    }

    @Bean
    public StringSerializer keySerializer() {
        return new StringSerializer();
    }

    @Bean
    public DefaultKafkaProducerFactory<String, Resource> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs(), keySerializer(), valueSerializer());
    }

    @Bean
    public KafkaTemplate<String, Resource> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

}
