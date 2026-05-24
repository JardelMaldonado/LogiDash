package com.jardel.LogiDash.controller;

import com.jardel.LogiDash.dto.abastecimento.Abastecimento;
import com.jardel.LogiDash.service.AbastecimentoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/abastecimentos")
public class AbastecimentoController {

    private final AbastecimentoService abastecimentoService;

    public AbastecimentoController(AbastecimentoService abastecimentoService) {
        this.abastecimentoService = abastecimentoService;
    }
    @GetMapping("/consultar")
    public ResponseEntity<List<Abastecimento>> consultar(
            @RequestParam String dataInicio,
            @RequestParam String dataFim) {
        return ResponseEntity.ok(abastecimentoService.buscarAbastecimentos(dataInicio, dataFim));
    }
}

