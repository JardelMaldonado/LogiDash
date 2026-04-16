package com.jardel.LogiDash.service;

import com.jardel.LogiDash.dto.AbastecimentoRequest;
import com.jardel.LogiDash.dto.AbastecimentoResponse;
import com.jardel.LogiDash.dto.ProFrotasResult;
import com.jardel.LogiDash.exception.ApiIndisponivelException;
import com.jardel.LogiDash.exception.RateLimitException;
import com.jardel.LogiDash.exception.SleepInterrompidoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;
import java.util.List;
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
                .filter(x -> x.getAbastecimentoEstornado() != null)
                .map(AbastecimentoResponse::getAbastecimentoEstornado)
                .map(Long::valueOf)
                .collect(Collectors.toSet());

        return todosRegistros.stream()
                .filter(x -> x.getMotivoRecusa() == null || x.getMotivoRecusa().isBlank())
                .filter(x -> x.getValorTotalCalculado() != null && x.getValorTotalCalculado().doubleValue() > 0)
                .filter(x -> x.getItensLista() != null && !x.getItensLista().isEmpty())
                .filter(x -> x.getData() != null && x.getData().compareTo(dataInicio) >= 0)
                .filter(x -> !idsEstornados.contains(x.getIdentificador()))
                .sorted((a, b) -> b.getData().compareTo(a.getData()))
                .toList();

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
