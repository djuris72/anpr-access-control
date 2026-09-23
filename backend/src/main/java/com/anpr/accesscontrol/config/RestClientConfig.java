package com.anpr.accesscontrol.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * RestTemplate koji koristimo za pozive ka ML servisu (FastAPI).
 * Timeout je namerno relativno kratak - ako ML servis ne odgovori brzo,
 * bolje je da kapija odbije prolaz i pokusa ponovo, nego da ceka predugo.
 */
@Configuration
public class RestClientConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(3))
                .setReadTimeout(Duration.ofSeconds(10)) // YOLO+EasyOCR inferencija na CPU moze potrajati par sekundi
                .build();
    }
}