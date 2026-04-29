package com.jardel.LogiDash.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record ItemAbastecimento(
        String nome,
        BigDecimal quantidade,
        @JsonProperty("valorUnitario")
        BigDecimal valorUnitario,
        @JsonProperty("valorTotal")
        BigDecimal valorTotal
) {}
