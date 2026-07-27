package com.jardel.LogiDash.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.jardel.LogiDash.scheduler.ImportacaoScheduler;
import com.jardel.LogiDash.service.ProFrotasService;
import com.jardel.LogiDash.dto.abastecimento.AbastecimentoResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
@Import(WireMockConfig.class)
class ProFrotasServiceIT {

    private static final String ENDPOINT = "/api/frotista/abastecimento/pesquisa";

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @MockitoBean
    private ImportacaoScheduler importacaoScheduler;

    @Autowired
    private WireMockServer wireMockServer;

    @Autowired
    private ProFrotasService proFrotasService;

    @AfterEach
    void resetStubs() {
        wireMockServer.resetAll();
    }

    @Test
    void deveBuscarERetornarAbastecimentoValido() {
        wireMockServer.stubFor(post(urlEqualTo(ENDPOINT))
                .inScenario("paginacao")
                .whenScenarioStateIs(STARTED)
                .willSetStateTo("pagina-2")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                    {
                      "registros": [
                        {
                          "identificador": 1,
                          "data": "2026-07-15T10:00:00",
                          "motivoRecusa": null,
                          "abastecimentoEstornado": null,
                          "veiculo": { "placa": "ABC1234" },
                          "motorista": { "nome": "João Silva" },
                          "items": [
                            { "nome": "Diesel S10", "quantidade": 50.0, "valorUnitario": 5.89, "valorTotal": 294.50 }
                          ]
                        }
                      ]
                    }
                    """)));

        wireMockServer.stubFor(post(urlEqualTo(ENDPOINT))
                .inScenario("paginacao")
                .whenScenarioStateIs("pagina-2")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                    { "registros": [] }
                    """)));

        List<AbastecimentoResponse> resultado =
                proFrotasService.buscarAbastecimentos("2026-07-01", "2026-07-31");

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().getIdentificador()).isEqualTo(1L);
    }

    @Test
    void deveRetentarQuandoRecebe429() {
        wireMockServer.stubFor(post(urlEqualTo(ENDPOINT))
                .inScenario("rate-limit")
                .whenScenarioStateIs(STARTED)
                .willSetStateTo("segunda-tentativa")
                .willReturn(aResponse().withStatus(429)));

        wireMockServer.stubFor(post(urlEqualTo(ENDPOINT))
                .inScenario("rate-limit")
                .whenScenarioStateIs("segunda-tentativa")
                .willSetStateTo("pagina-2")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                    {
                      "registros": [
                        {
                          "identificador": 5,
                          "data": "2026-07-15T10:00:00",
                          "motivoRecusa": null,
                          "abastecimentoEstornado": null,
                          "veiculo": { "placa": "XYZ9999" },
                          "motorista": { "nome": "Maria" },
                          "items": [
                            { "nome": "Diesel", "quantidade": 30.0, "valorUnitario": 6.0, "valorTotal": 180.0 }
                          ]
                        }
                      ]
                    }
                    """)));

        wireMockServer.stubFor(post(urlEqualTo(ENDPOINT))
                .inScenario("rate-limit")
                .whenScenarioStateIs("pagina-2")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                    { "registros": [] }
                    """)));

        List<AbastecimentoResponse> resultado =
                proFrotasService.buscarAbastecimentos("2026-07-01", "2026-07-31");

        assertThat(resultado).hasSize(1);
        wireMockServer.verify(3, postRequestedFor(urlEqualTo(ENDPOINT)));
    }
}