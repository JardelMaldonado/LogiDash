package com.jardel.LogiDash.dto.abastecimento;

import java.time.LocalDateTime;
import java.util.List;

public record Abastecimento(
        Long identificador,
        LocalDateTime data,
        String placa,
        String nomeMotorista,
        String razaoSocialPosto,
        boolean postoInterno,
        List<AbastecimentoItem> itens
) {}
