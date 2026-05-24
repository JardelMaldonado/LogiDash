package com.jardel.LogiDash.dto.abastecimento;

import java.math.BigDecimal;

public record AbastecimentoItem(
        String tipoCombustivel,
        BigDecimal quantidade,
        BigDecimal valorUnitario,
        BigDecimal valorTotal
) {}
