package com.example.poc.kafka.sleuth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class SerdeConfig {

    @Bean
    public JsonSerde<Resource> resourceSerde() {
        Map<String, Object> props = new HashMap<>();
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, "false");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, Resource.class);

        JsonSerde<Resource> serde = new JsonSerde<>(Resource.class);
        serde.configure(props, false);

        return serde;
    }

}
