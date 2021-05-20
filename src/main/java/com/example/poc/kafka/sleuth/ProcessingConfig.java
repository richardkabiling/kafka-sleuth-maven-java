package com.example.poc.kafka.sleuth;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.util.function.Consumer;

@Slf4j
@Configuration
public class ProcessingConfig {

    @Bean
    public Consumer<KStream<String, Resource>> materializeResources(JsonSerde<Resource> resourceSerde) {
        return it -> it.peek((key, resource) -> log.info("Received request to materialize resource. key={}, value={}", key, resource))
                .toTable(Materialized.<String, Resource, KeyValueStore<Bytes, byte[]>>as("resources-store")
                        .withKeySerde(new Serdes.StringSerde())
                        .withValueSerde(resourceSerde));
    }

}
