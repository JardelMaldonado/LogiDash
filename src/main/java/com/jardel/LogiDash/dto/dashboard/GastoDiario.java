package com.jardel.LogiDash.dto.dashboard;

import java.math.BigDecimal;

public record GastoDiario(
        String dia,
        BigDecimal valor,
        BigDecimal litros
) {}
