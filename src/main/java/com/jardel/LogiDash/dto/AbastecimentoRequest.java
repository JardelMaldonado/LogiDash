package com.jardel.LogiDash.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AbastecimentoRequest(
        Integer pagina,
        Integer tamanhoPagina,
        String dataInicial,
        String dataFinal,
        Boolean postoInterno,
        String placaVeiculo
) {}
