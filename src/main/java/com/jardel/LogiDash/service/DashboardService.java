package com.jardel.LogiDash.service;

import com.jardel.LogiDash.database.model.AbastecimentoEntity;
import com.jardel.LogiDash.database.model.AbastecimentoItemEntity;
import com.jardel.LogiDash.dto.dashboard.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final Logger log = LoggerFactory.getLogger(DashboardService.class);
    private static final int LIMITE_BALDE = 5;

    private final AbastecimentoService abastecimentoService;

    public DashboardResponse calcular(String dataInicial, String dataFinal, String placa, String motorista) {
        List<AbastecimentoEntity> todos = abastecimentoService.buscarEntities(dataInicial, dataFinal);

        List<String> todasPlacas = todos.stream()
                .map(AbastecimentoEntity::getPlaca)
                .filter(p -> p != null && !p.isBlank())
                .distinct()
                .sorted()
                .toList();

        List<String> todosMotoristas = todos.stream()
                .map(AbastecimentoEntity::getNomeMotorista)
                .filter(m -> m != null && !m.isBlank())
                .distinct()
                .sorted()
                .toList();

        if (placa != null && !placa.isBlank()) {
            todos = todos.stream()
                    .filter(x -> placa.equals(x.getPlaca()))
                    .toList();
        }
        if (motorista != null && !motorista.isBlank()) {
            todos = todos.stream()
                    .filter(x -> motorista.equals(x.getNomeMotorista()))
                    .toList();
        }

        List<AbastecimentoEntity> interno = todos.stream()
                .filter(AbastecimentoEntity::isPostoInterno)
                .toList();

        List<AbastecimentoEntity> externo = todos.stream()
                .filter(x -> !x.isPostoInterno())
                .toList();

        return new DashboardResponse(
                calcularTotalGeral(todos),
                calcularTotalLitros(todos),
                todos.size(),
                calcularConsumo(interno, externo),
                calcularPrecoMedio(interno, externo),
                calcularRankingPostos(todos),
                calcularRankingMotoristas(todos),
                calcularGastoDiario(todos),
                calcularPrecoDieselDiario(externo),
                todasPlacas,
                todosMotoristas
        );
    }

    private BigDecimal calcularTotalGeral(List<AbastecimentoEntity> lista) {
        return lista.stream()
                .map(x -> x.getValorTotalCalculado() != null ? x.getValorTotalCalculado() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calcularTotalLitros(List<AbastecimentoEntity> lista) {
        return lista.stream()
                .map(x -> x.getTotalLitros() != null ? x.getTotalLitros() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal somaLitrosPorNome(List<AbastecimentoEntity> lista, String nomeContem) {
        return lista.stream()
                .filter(a -> a.getItens() != null)
                .flatMap(a -> a.getItens().stream())
                .filter(i -> i.getTipoCombustivel() != null && i.getTipoCombustivel().toLowerCase().contains(nomeContem))
                .map(i -> i.getQuantidade() != null ? i.getQuantidade() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private Consumo calcularConsumo(List<AbastecimentoEntity> interno, List<AbastecimentoEntity> externo) {
        return new Consumo(
                calcularTotalLitros(interno),
                calcularTotalLitros(externo),
                somaLitrosPorNome(interno, "diesel"),
                somaLitrosPorNome(interno, "arla 32 - granel"),
                somaLitrosPorNome(externo, "diesel"),
                somaLitrosGranelComBaldesSuspeitos(externo),
                somaLitrosBalde(externo, "arla 32 - balde"),
                somaLitrosPorNome(externo, "gasolina")
        );
    }

    private PrecoMedio calcularPrecoMedio(List<AbastecimentoEntity> interno, List<AbastecimentoEntity> externo) {
        return new PrecoMedio(
                mediaPreco(interno, "diesel"),
                mediaPreco(externo, "diesel"),
                mediaPreco(externo, "arla 32 - granel"),
                mediaPrecoBaldeParaLitro(externo, "arla 32 - balde"),
                mediaPreco(externo, "gasolina"),
                calcularTotalGeral(interno),
                calcularTotalGeral(externo)
        );
    }

    private List<RankingItem> calcularRankingPostos(List<AbastecimentoEntity> lista) {
        Map<String, BigDecimal[]> mapa = new LinkedHashMap<>();

        for (AbastecimentoEntity a : lista) {
            String nome = a.getRazaoSocialPosto() != null ? a.getRazaoSocialPosto().trim() : "Desconhecido";
            mapa.putIfAbsent(nome, new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO});
            BigDecimal[] v = mapa.get(nome);
            v[0] = v[0].add(a.getTotalLitros() != null ? a.getTotalLitros() : BigDecimal.ZERO);
            v[1] = v[1].add(a.getValorTotalCalculado() != null ? a.getValorTotalCalculado() : BigDecimal.ZERO);
            if (a.getItens() != null) {
                for (AbastecimentoItemEntity item : a.getItens()) {
                    if (item.getTipoCombustivel() != null && item.getTipoCombustivel().toLowerCase().contains("diesel")) {
                        v[2] = v[2].add(item.getValorUnitario() != null ? item.getValorUnitario() : BigDecimal.ZERO);
                        v[3] = v[3].add(BigDecimal.ONE);
                    }
                }
            }
        }

        return mapa.entrySet().stream()
                .map(e -> {
                    BigDecimal[] v = e.getValue();
                    BigDecimal precoDiesel = v[3].compareTo(BigDecimal.ZERO) > 0
                            ? v[2].divide(v[3], 2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;
                    return new RankingItem(e.getKey(),
                            v[0].setScale(2, RoundingMode.HALF_UP),
                            v[1].setScale(2, RoundingMode.HALF_UP),
                            precoDiesel, null, null);
                })
                .sorted(Comparator.comparing(RankingItem::litros).reversed())
                .limit(10)
                .toList();
    }

    private List<RankingItem> calcularRankingMotoristas(List<AbastecimentoEntity> lista) {
        Map<String, Object[]> mapa = new LinkedHashMap<>();

        for (AbastecimentoEntity a : lista) {
            String nome = a.getNomeMotorista() != null ? a.getNomeMotorista().trim() : "Desconhecido";
            mapa.putIfAbsent(nome, new Object[]{BigDecimal.ZERO, BigDecimal.ZERO, 0, new HashMap<String, Integer>()});
            Object[] v = mapa.get(nome);
            v[0] = ((BigDecimal) v[0]).add(a.getTotalLitros() != null ? a.getTotalLitros() : BigDecimal.ZERO);
            v[1] = ((BigDecimal) v[1]).add(a.getValorTotalCalculado() != null ? a.getValorTotalCalculado() : BigDecimal.ZERO);
            v[2] = (int) v[2] + 1;
            String placaMotorista = a.getPlaca() != null ? a.getPlaca() : "N/A";
            @SuppressWarnings("unchecked")
            Map<String, Integer> placas = (Map<String, Integer>) v[3];
            placas.merge(placaMotorista, 1, Integer::sum);
        }

        return mapa.entrySet().stream()
                .map(e -> {
                    Object[] v = e.getValue();
                    @SuppressWarnings("unchecked")
                    Map<String, Integer> placas = (Map<String, Integer>) v[3];
                    String placaPrincipal = placas.entrySet().stream()
                            .max(Map.Entry.comparingByValue())
                            .map(Map.Entry::getKey)
                            .orElse("N/A");
                    return new RankingItem(e.getKey(),
                            ((BigDecimal) v[0]).setScale(2, RoundingMode.HALF_UP),
                            ((BigDecimal) v[1]).setScale(2, RoundingMode.HALF_UP),
                            null, (int) v[2], placaPrincipal);
                })
                .sorted(Comparator.comparing(RankingItem::litros).reversed())
                .limit(10)
                .toList();
    }

    private List<GastoDiario> calcularGastoDiario(List<AbastecimentoEntity> lista) {
        Map<String, BigDecimal[]> mapa = new TreeMap<>();
        for (AbastecimentoEntity a : lista) {
            if (a.getData() == null) continue;
            String dia = a.getData().toLocalDate().toString();
            mapa.putIfAbsent(dia, new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            BigDecimal[] v = mapa.get(dia);
            v[0] = v[0].add(a.getValorTotalCalculado() != null ? a.getValorTotalCalculado() : BigDecimal.ZERO);
            v[1] = v[1].add(a.getTotalLitros() != null ? a.getTotalLitros() : BigDecimal.ZERO);
        }
        return mapa.entrySet().stream()
                .map(e -> new GastoDiario(e.getKey(),
                        e.getValue()[0].setScale(2, RoundingMode.HALF_UP),
                        e.getValue()[1].setScale(2, RoundingMode.HALF_UP)))
                .toList();
    }

    private List<PrecoDiario> calcularPrecoDieselDiario(List<AbastecimentoEntity> externo) {
        Map<String, BigDecimal[]> mapa = new TreeMap<>();

        for (AbastecimentoEntity a : externo) {
            if (a.getData() == null || a.getItens() == null) continue;
            String dia = a.getData().toLocalDate().toString();
            for (AbastecimentoItemEntity item : a.getItens()) {
                if (item.getTipoCombustivel() != null && item.getTipoCombustivel().toLowerCase().contains("diesel")) {
                    mapa.putIfAbsent(dia, new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
                    BigDecimal[] v = mapa.get(dia);
                    v[0] = v[0].add(item.getValorUnitario() != null ? item.getValorUnitario() : BigDecimal.ZERO);
                    v[1] = v[1].add(BigDecimal.ONE);
                }
            }
        }
        return mapa.entrySet().stream()
                .filter(e -> e.getValue()[1].compareTo(BigDecimal.ZERO) > 0)
                .map(e -> new PrecoDiario(e.getKey(),
                        e.getValue()[0].divide(e.getValue()[1], 4, RoundingMode.HALF_UP)))
                .toList();
    }

    private BigDecimal somaLitrosGranelComBaldesSuspeitos(List<AbastecimentoEntity> lista) {
        BigDecimal granel = somaLitrosPorNome(lista, "arla 32 - granel");
        BigDecimal baldeSuspeito = lista.stream()
                .filter(a -> a.getItens() != null)
                .flatMap(a -> a.getItens().stream())
                .filter(i -> i.getTipoCombustivel() != null && i.getTipoCombustivel().toLowerCase().contains("arla 32 - balde"))
                .filter(i -> i.getQuantidade() != null && i.getQuantidade().compareTo(BigDecimal.valueOf(LIMITE_BALDE)) > 0)
                .map(AbastecimentoItemEntity::getQuantidade)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        return granel.add(baldeSuspeito);
    }

    private BigDecimal somaLitrosBalde(List<AbastecimentoEntity> lista, String nomeContem) {
        return lista.stream()
                .filter(a -> a.getItens() != null)
                .flatMap(a -> a.getItens().stream())
                .filter(i -> i.getTipoCombustivel() != null && i.getTipoCombustivel().toLowerCase().contains(nomeContem))
                .filter(i -> i.getQuantidade() != null && i.getQuantidade().compareTo(BigDecimal.valueOf(LIMITE_BALDE)) <= 0)
                .map(i -> i.getQuantidade().multiply(BigDecimal.valueOf(20)))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal mediaPreco(List<AbastecimentoEntity> lista, String nomeContem) {
        List<BigDecimal> precos = lista.stream()
                .filter(a -> a.getItens() != null)
                .flatMap(a -> a.getItens().stream())
                .filter(i -> i.getTipoCombustivel() != null && i.getTipoCombustivel().toLowerCase().contains(nomeContem))
                .map(AbastecimentoItemEntity::getValorUnitario)
                .filter(v -> v != null && v.compareTo(BigDecimal.ZERO) > 0)
                .toList();
        if (precos.isEmpty()) return BigDecimal.ZERO;
        BigDecimal soma = precos.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return soma.divide(BigDecimal.valueOf(precos.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal mediaPrecoBaldeParaLitro(List<AbastecimentoEntity> lista, String nomeContem) {
        BigDecimal media = mediaPreco(lista, nomeContem);
        if (media.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return media.divide(BigDecimal.valueOf(20), 2, RoundingMode.HALF_UP);
    }
}