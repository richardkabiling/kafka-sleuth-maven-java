package com.example.poc.kafka.sleuth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.state.HostInfo;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.util.concurrent.ExecutionException;

@Slf4j
@Controller
public class ResourceController {

    @Autowired
    private KafkaTemplate<String, Resource> kafkaTemplate;

    @Autowired
    private WebClient webClient;

    @Autowired
    private InteractiveQueryService queryService;

    @PutMapping("/namespaces/{namespace}/resources/{resourceId}")
    public ResponseEntity<Resource> mapResource(@RequestBody MapIssuerRequest request,
                                                @PathVariable("namespace") String namespace,
                                                @PathVariable("resourceId") String resourceId) throws ExecutionException, InterruptedException {
        log.info("Received request to map resource. namespace={}, resourceId={}. request={}", namespace, resourceId, request);

        Resource resource = Resource.builder()
                .namespace(namespace)
                .id(resourceId)
                .name(request.getName())
                .build();
        kafkaTemplate.send("resources", namespace + ":" + resourceId, resource)
                .get();

        return ResponseEntity.ok(resource);
    }

    @GetMapping("/namespaces/{namespace}/resources/{resourceId}")
    public ResponseEntity<Resource> getResource(@PathVariable("namespace") String namespace,
                          @PathVariable("resourceId") String resourceId) {
        log.info("Received request to get resource. namespace={}, resourceId={}", namespace, resourceId);

        String key = namespace + ":" + resourceId;
        HostInfo targetHostInfo = queryService.getHostInfo("resources-store", key, new StringSerializer());

        log.info("currentHostInfo={}", queryService.getCurrentHostInfo());
        log.info("targetHostInfo={}", targetHostInfo);

        if(targetHostInfo.equals(queryService.getCurrentHostInfo())) {
            log.info("Target host is local. targetHost=local");

            ReadOnlyKeyValueStore<String, Resource> resourcesStore = queryService.getQueryableStore("resources-store",
                    QueryableStoreTypes.keyValueStore());
            Resource resource = resourcesStore.get(key);

            log.info("Returning resource. resource={}", resource);

            if(resource == null) {
                throw new IllegalStateException();
            } else {
                return ResponseEntity.ok(resource);
            }
        } else {
            log.info("Target host is remote. targetHost=remote");
            Resource resource = webClient.get()
                    .uri(URI.create("http://" + targetHostInfo.host() +":" + targetHostInfo.port() + "/namespaces/" + namespace + "/resources/" + resourceId))
                    .exchangeToMono(it -> it.bodyToMono(Resource.class))
                    .block();

            return ResponseEntity.ok(resource);
        }
    }
}

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
class MapIssuerRequest {
    private String name;
}

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
class Resource {
    private String namespace;
    private String id;
    private String name;
}