package com.jardel.LogiDash.dto.dashboard;

import java.math.BigDecimal;

public record RankingItem(
        String nome,
        BigDecimal litros,
        BigDecimal valor,
        BigDecimal precoDiesel,
        Integer abastecimentos,
        String placaPrincipal
) {}
