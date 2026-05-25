package com.jardel.LogiDash.controller;

import com.jardel.LogiDash.dto.abastecimento.Abastecimento;
import com.jardel.LogiDash.service.AbastecimentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/abastecimentos")
@RequiredArgsConstructor
public class AbastecimentoController {

    private final AbastecimentoService abastecimentoService;

    @GetMapping("/consultar")
    public ResponseEntity<List<Abastecimento>> consultar(
            @RequestParam String dataInicio,
            @RequestParam String dataFim) {
        return ResponseEntity.ok(abastecimentoService.buscarAbastecimentos(dataInicio, dataFim));
    }
}

