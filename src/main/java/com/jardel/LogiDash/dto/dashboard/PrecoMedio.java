package com.jardel.LogiDash.dto.dashboard;

import java.math.BigDecimal;

public record PrecoMedio(
        BigDecimal dieselInterno,
        BigDecimal dieselExterno,
        BigDecimal arlaGranelExterno,
        BigDecimal arlaBaldeExterno,
        BigDecimal gasolinaExterno,
        BigDecimal valorInternoTotal,
        BigDecimal valorExternoTotal
) {}
