package com.jardel.LogiDash.dto.dashboard;

import java.math.BigDecimal;

public record Consumo(
        BigDecimal litrosInterno,
        BigDecimal litrosExterno,
        BigDecimal dieselInterno,
        BigDecimal arlaGranelInterno,
        BigDecimal dieselExterno,
        BigDecimal arlaGranelExterno,
        BigDecimal arlaBaldeExterno,
        BigDecimal gasolinaExterno
) { }
