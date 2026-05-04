package com.jardel.LogiDash.dto.abastecimento;

public record AbastecimentoRequest(
        Integer pagina,
        Integer tamanhoPagina,
        String dataInicial,
        String dataFinal,
        Boolean postoInterno,
        String placaVeiculo
) {}
