package com.jardel.LogiDash.integration;

import com.jardel.LogiDash.database.model.UsuarioEntity;
import com.jardel.LogiDash.database.repository.IUsuarioRepository;
import com.jardel.LogiDash.dto.auth.LoginRequest;
import com.jardel.LogiDash.dto.auth.LoginResponsePublico;
import com.jardel.LogiDash.scheduler.ImportacaoScheduler;
import org.junit.jupiter.api.AfterEach;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@Testcontainers
@ActiveProfiles("test")
@Import(WireMockConfig.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
public class AuthControllerIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private IUsuarioRepository repository;

    @Autowired
    private RestTestClient restTestClient;

    @MockitoBean
    private ImportacaoScheduler importacaoScheduler;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @AfterEach
    void limparBanco() {
        repository.deleteAll();
    }

    @Test
    void quandoCredenciaisValidas_deveRetornarDadosDoUsuarioEStatus200() {

        var usuario = UsuarioEntity.builder()
                .nome("Jardel")
                .email("jardel@example.com")
                .senha(passwordEncoder.encode("123456"))
                .role(UsuarioEntity.Role.USER)
                 .ativo(true)
                .build();

        repository.save(usuario);

        restTestClient
                .post()
                .uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new LoginRequest("jardel@example.com", "123456"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(LoginResponsePublico.class)
                .value(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.nome()).isEqualTo("Jardel");
                    assertThat(response.email()).isEqualTo("jardel@example.com");
                    assertThat(response.role()).isEqualTo("USER");
                });
    }

    @Test
    void quandoCredenciaisInvalidas_deveRetornarErroEStatus401() {

        var usuario = UsuarioEntity.builder()
                .nome("Jardel")
                .email("jardel@example.com")
                .senha(passwordEncoder.encode("123456"))
                .role(UsuarioEntity.Role.USER)
                .ativo(true)
                .build();

        repository.save(usuario);

        restTestClient
                .post()
                .uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new LoginRequest("jardel@example.com", "12345"))
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody(Map.class)
                .value(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.get("erro")).isEqualTo("Email ou senha incorretos");

                });
    }
    @Test
    void quandoCookieValido_deveAutenticar() {

        var usuario = UsuarioEntity.builder()
                .nome("Jardel")
                .email("jardel@example.com")
                .senha(passwordEncoder.encode("123456"))
                .role(UsuarioEntity.Role.USER)
                .ativo(true)
                .build();

        repository.save(usuario);

        restTestClient
                .post()
                .uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new LoginRequest("jardel@example.com", "123456"))
                .exchange()
                .expectStatus().isOk()
                .expectCookie().exists("auth_token")
                .expectCookie().httpOnly("auth_token", true)
                .expectCookie().secure("auth_token", true)
                .expectCookie().sameSite("auth_token", "None")
                .expectCookie().path("auth_token", "/")
                .expectBody(LoginResponsePublico.class)
                .value(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.nome()).isEqualTo("Jardel");
                    assertThat(response.email()).isEqualTo("jardel@example.com");
                    assertThat(response.role()).isEqualTo("USER");
                });
    }

    @Test
    void quandoFeitoLogout_deveApagarCookie() {

        restTestClient
                .post()
                .uri("/api/v1/auth/logout")
                .exchange()
                .expectStatus().isOk()
                .expectCookie().maxAge("auth_token", Duration.ZERO);

    }
}
