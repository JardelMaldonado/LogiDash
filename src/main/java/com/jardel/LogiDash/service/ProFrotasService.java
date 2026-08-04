package com.jardel.LogiDash.service;

import com.jardel.LogiDash.dto.abastecimento.AbastecimentoRequest;
import com.jardel.LogiDash.dto.abastecimento.AbastecimentoResponse;
import com.jardel.LogiDash.dto.abastecimento.ProFrotasResult;
import com.jardel.LogiDash.exception.ApiIndisponivelException;
import com.jardel.LogiDash.exception.SleepInterrompidoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProFrotasService {

    private static final Logger log = LoggerFactory.getLogger(ProFrotasService.class);
    private static final int TAMANHO_PAGINA = 100;
    private static final int MAX_PAGINAS = 100;
    private static final long DELAY_ENTRE_PAGINAS_MS = 1000;
    private static final long DELAY_RATE_LIMIT_MS = 5000;

    private final WebClient webClient;

    public ProFrotasService(WebClient webClient) {
        this.webClient = webClient;
    }

    public List<AbastecimentoResponse> buscarAbastecimentos(String dataInicio, String dataFim) {
        List<AbastecimentoResponse> todosRegistros = new ArrayList<>();
        int paginaAtual = 1;
        boolean temMaisDados = true;

        String inicioFormatado = dataInicio.contains("T") ? dataInicio : dataInicio + "T00:00:00";
        String fimFormatado = dataFim.contains("T") ? dataFim : dataFim + "T23:59:59";

        while (temMaisDados && paginaAtual <= MAX_PAGINAS) {
            log.info("Buscando página {}...", paginaAtual);

            AbastecimentoRequest filtro = new AbastecimentoRequest(
                    paginaAtual,
                    TAMANHO_PAGINA,
                    inicioFormatado,
                    fimFormatado,
                    null,
                    null
            );

            try {
                ProFrotasResult result = this.webClient.post()
                        .uri("/api/frotista/abastecimento/pesquisa")
                        .bodyValue(filtro)
                        .retrieve()
                        .bodyToMono(ProFrotasResult.class)
                        .timeout(Duration.ofSeconds(30))
                        .block();

                if (result != null && result.registros() != null && !result.registros().isEmpty()) {
                    todosRegistros.addAll(result.registros());
                    paginaAtual++;
                    aguardar(DELAY_ENTRE_PAGINAS_MS);
                } else {
                    temMaisDados = false;
                }

            } catch (WebClientResponseException e) {
                if (e.getStatusCode().value() == 429) {
                    log.warn("Rate limit atingido na página {}, aguardando...", paginaAtual);
                    aguardar(DELAY_RATE_LIMIT_MS);
                } else {
                    throw new ApiIndisponivelException("API ProFrotas indisponível: " + e.getMessage());
                }
            }
        }

        Set<Long> idsEstornados = todosRegistros.stream()
                .map(AbastecimentoResponse::getAbastecimentoEstornado)
                .filter(Objects::nonNull)
                .map(Long::valueOf)
                .collect(Collectors.toSet());

        return todosRegistros.stream()
                .filter(x -> isAbastecimentoValido(x, dataInicio, idsEstornados))
                .toList();
    }

    private boolean isAbastecimentoValido(AbastecimentoResponse x, String dataInicio, Set<Long> idsEstornados) {
        return (x.getMotivoRecusa() == null || x.getMotivoRecusa().isBlank()) &&
                (!idsEstornados.contains(x.getIdentificador())) &&
                (x.getItensLista() != null && !x.getItensLista().isEmpty()) &&
                (x.getData() != null && x.getData().substring(0, 10).compareTo(dataInicio) >= 0) &&
                (x.getItensLista().stream()
                        .anyMatch(item -> item.quantidade() != null
                                && item.quantidade().compareTo(BigDecimal.ZERO) > 0));
    }

    private void aguardar(long milissegundos) {
        try {
            Thread.sleep(milissegundos);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SleepInterrompidoException("Espera interrompida durante a busca de abastecimentos", e);
        }
    }
}