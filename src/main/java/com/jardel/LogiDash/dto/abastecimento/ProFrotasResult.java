package com.jardel.LogiDash.dto.abastecimento;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ProFrotasResult(
        @JsonProperty("registros") List<AbastecimentoResponse> registros
) {}
