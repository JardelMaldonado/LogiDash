package com.jardel.LogiDash.dto.dashboard;

import com.jardel.LogiDash.dto.dashboard.*;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResponse(
        BigDecimal totalGeral,
        BigDecimal totalLitros,
        Integer totalAbastecimentos,
        Consumo consumo,
        PrecoMedio precoMedio,
        List<RankingItem> rankingPostos,
        List<RankingItem> rankingMotoristas,
        List<GastoDiario> gastoDiario,
        List<PrecoDiario> precoDieselDiario,
        List<String> todasPlacas,
        List<String> todosMotoristas
) {}
