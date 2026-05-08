package com.jardel.LogiDash.dto.dashboard;

import java.math.BigDecimal;

public record PrecoDiario(
        String dia,
        BigDecimal preco
) {}
