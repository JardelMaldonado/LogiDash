package com.jardel.LogiDash.service;

import com.jardel.LogiDash.dto.AbastecimentoRequest;
import com.jardel.LogiDash.dto.AbastecimentoResponse;
import com.jardel.LogiDash.dto.ProFrotasResult;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProFrotasService {

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

        while (temMaisDados && paginaAtual <= 100) {
            System.out.println("Buscando Página " + paginaAtual + "...");

            AbastecimentoRequest filtro = new AbastecimentoRequest(
                    paginaAtual,
                    100,
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
                    Thread.sleep(500);
                } else {
                    temMaisDados = false;
                }
            } catch (Exception e) {
                System.err.println("Erro na chamada: " + e.getMessage());
                temMaisDados = false;
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
}
