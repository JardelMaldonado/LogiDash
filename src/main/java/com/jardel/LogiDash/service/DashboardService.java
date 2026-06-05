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
        Map<String, PostoAgregado> mapa = new LinkedHashMap<>();

        for (AbastecimentoEntity a : lista) {
            String nome = a.getRazaoSocialPosto() != null ? a.getRazaoSocialPosto().trim() : "Desconhecido";

            mapa.putIfAbsent(nome, new PostoAgregado());
            PostoAgregado posto = mapa.get(nome);

            posto.totalLitros = posto.totalLitros.add(a.getTotalLitros() != null ? a.getTotalLitros() : BigDecimal.ZERO);
            posto.totalGasto = posto.totalGasto.add(a.getValorTotalCalculado() != null ? a.getValorTotalCalculado() : BigDecimal.ZERO);

            if (a.getItens() != null) {
                for (AbastecimentoItemEntity item : a.getItens()) {
                    if (item.getTipoCombustivel() != null && item.getTipoCombustivel().toLowerCase().contains("diesel")) {
                        posto.somaPrecoDiesel = posto.somaPrecoDiesel.add(item.getValorUnitario() != null ? item.getValorUnitario() : BigDecimal.ZERO);
                        posto.contagemDiesel = posto.contagemDiesel.add(BigDecimal.ONE);
                    }
                }
            }
        }

        return mapa.entrySet().stream()
                .map(e -> {
                    PostoAgregado posto = e.getValue();
                    BigDecimal precoDiesel = posto.contagemDiesel.compareTo(BigDecimal.ZERO) > 0 ? posto.somaPrecoDiesel.divide(posto.contagemDiesel, 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
                    return new RankingItem(
                            e.getKey(),
                            posto.totalLitros.setScale(2, RoundingMode.HALF_UP),
                            posto.totalGasto.setScale(2, RoundingMode.HALF_UP),
                            precoDiesel, null, null);
                })
                .sorted(Comparator.comparing(RankingItem::litros).reversed())
                .limit(10)
                .toList();
    }

    private List<RankingItem> calcularRankingMotoristas(List<AbastecimentoEntity> lista) {
        Map<String, MotoristaAgregado> mapa = new LinkedHashMap<>();

        for (AbastecimentoEntity a : lista) {
            String nome = a.getNomeMotorista() != null ? a.getNomeMotorista().trim() : "Desconhecido";

            mapa.putIfAbsent(nome, new MotoristaAgregado());
            MotoristaAgregado motorista = mapa.get(nome);

            motorista.totalLitros = motorista.totalLitros.add(a.getTotalLitros() != null ? a.getTotalLitros() : BigDecimal.ZERO);
            motorista.totalGasto = motorista.totalGasto.add(a.getValorTotalCalculado() != null ? a.getValorTotalCalculado() : BigDecimal.ZERO);
            motorista.abastecimentos++;

            String placaMotorista = a.getPlaca() != null ? a.getPlaca() : "N/A";
            motorista.placas.merge(placaMotorista, 1, Integer::sum);
        }

        return mapa.entrySet().stream()
                .map(e -> {
                    MotoristaAgregado motorista = e.getValue();
                    String placaPrincipal = motorista.placas.entrySet().stream()
                            .max(Map.Entry.comparingByValue())
                            .map(Map.Entry::getKey)
                            .orElse("N/A");
                    return new RankingItem(
                            e.getKey(),
                            motorista.totalLitros.setScale(2, RoundingMode.HALF_UP),
                            motorista.totalGasto.setScale(2, RoundingMode.HALF_UP),
                            null, motorista.abastecimentos, placaPrincipal);
                })
                .sorted(Comparator.comparing(RankingItem::litros).reversed())
                .limit(10)
                .toList();
    }

    private List<GastoDiario> calcularGastoDiario(List<AbastecimentoEntity> lista) {
        Map<String, DiaAgregado> mapa = new TreeMap<>();

        for (AbastecimentoEntity a : lista) {
            if (a.getData() == null) continue;
            String dia = a.getData().toLocalDate().toString();

            mapa.putIfAbsent(dia, new DiaAgregado());
            DiaAgregado diaAgregado = mapa.get(dia);

            diaAgregado.totalGasto = diaAgregado.totalGasto.add(a.getValorTotalCalculado() != null ? a.getValorTotalCalculado() : BigDecimal.ZERO);
            diaAgregado.totalLitros = diaAgregado.totalLitros.add(a.getTotalLitros() != null ? a.getTotalLitros() : BigDecimal.ZERO);
        }
        return mapa.entrySet().stream()
                .map(e -> new GastoDiario(
                        e.getKey(),
                        e.getValue().totalGasto.setScale(2, RoundingMode.HALF_UP),
                        e.getValue().totalLitros.setScale(2, RoundingMode.HALF_UP)))
                .toList();
    }

    private List<PrecoDiario> calcularPrecoDieselDiario(List<AbastecimentoEntity> externo) {
        Map<String, DiaDieselAgregado> mapa = new TreeMap<>();

        for (AbastecimentoEntity a : externo) {
            if (a.getData() == null || a.getItens() == null) continue;
            String dia = a.getData().toLocalDate().toString();

            for (AbastecimentoItemEntity item : a.getItens()) {
                if (item.getTipoCombustivel() != null && item.getTipoCombustivel().toLowerCase().contains("diesel")) {
                    mapa.putIfAbsent(dia, new DiaDieselAgregado());
                    DiaDieselAgregado diaAgregado = mapa.get(dia);

                    diaAgregado.somaPrecoDiesel = diaAgregado.somaPrecoDiesel.add(item.getValorUnitario() != null ? item.getValorUnitario() : BigDecimal.ZERO);
                    diaAgregado.contagemDiesel = diaAgregado.contagemDiesel.add(BigDecimal.ONE);
                }
            }
        }
        return mapa.entrySet().stream()
                .filter(e -> e.getValue().contagemDiesel.compareTo(BigDecimal.ZERO) > 0)
                .map(e -> new PrecoDiario(
                        e.getKey(),
                        e.getValue().somaPrecoDiesel.divide(e.getValue().contagemDiesel, 4, RoundingMode.HALF_UP)))
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
        List<BigDecimal> precos = lista.stream()
                .filter(a -> a.getItens() != null)
                .flatMap(a -> a.getItens().stream())
                .filter(i -> i.getTipoCombustivel() != null && i.getTipoCombustivel().toLowerCase().contains(nomeContem))
                .filter(i -> i.getQuantidade() != null && i.getQuantidade().compareTo(BigDecimal.valueOf(LIMITE_BALDE)) <= 0)
                .filter(i -> i.getValorUnitario() != null && i.getValorUnitario().compareTo(BigDecimal.ZERO) > 0)
                .map(v -> v.getValorUnitario().divide(BigDecimal.valueOf(20), 4, RoundingMode.HALF_UP))
                .toList();
        if (precos.isEmpty()) return BigDecimal.ZERO;
        BigDecimal soma = precos.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return soma.divide(BigDecimal.valueOf(precos.size()), 4, RoundingMode.HALF_UP);
    }

    private static class PostoAgregado {
        BigDecimal totalLitros     = BigDecimal.ZERO;
        BigDecimal totalGasto      = BigDecimal.ZERO;
        BigDecimal somaPrecoDiesel = BigDecimal.ZERO;
        BigDecimal contagemDiesel  = BigDecimal.ZERO;
    }

    private static class MotoristaAgregado {
        BigDecimal totalLitros    = BigDecimal.ZERO;
        BigDecimal totalGasto     = BigDecimal.ZERO;
        int        abastecimentos = 0;
        Map<String, Integer> placas = new HashMap<>();
    }

    private static class DiaAgregado {
        BigDecimal totalGasto  = BigDecimal.ZERO;
        BigDecimal totalLitros = BigDecimal.ZERO;
    }

    private static class DiaDieselAgregado {
        BigDecimal somaPrecoDiesel = BigDecimal.ZERO;
        BigDecimal contagemDiesel  = BigDecimal.ZERO;
    }
}