package com.jardel.LogiDash.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;

@TestConfiguration
public class WireMockConfig {

    @Bean(initMethod = "start", destroyMethod = "stop")
    public WireMockServer wireMockServer() {
        return new WireMockServer(0);
    }

    @Bean
    public DynamicPropertyRegistrar profrotasApiUrl(WireMockServer wireMockServer) {
        return registry -> registry.add(
                "profrotas.api.url",
                () -> "http://localhost:" + wireMockServer.port()
        );
    }
}
