package com.jardel.LogiDash.controller;

import com.jardel.LogiDash.dto.AbastecimentoRequest;
import com.jardel.LogiDash.dto.AbastecimentoResponse;
import com.jardel.LogiDash.dto.ProFrotasResult;
import com.jardel.LogiDash.service.ProFrotasService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/abastecimentos")
@CrossOrigin(origins = "http://localhost:3000")
public class AbastecimentoController {

    private final ProFrotasService proFrotasService;

    public AbastecimentoController(ProFrotasService proFrotasService) {
        this.proFrotasService = proFrotasService;
    }

    @PostMapping("/consultar")
    public ResponseEntity<List<AbastecimentoResponse>> consultar(@RequestBody AbastecimentoRequest request) {
        System.out.println("Recebido: dataInicial=" + request.dataInicial() + " dataFinal=" + request.dataFinal());

        List<AbastecimentoResponse> resultado = proFrotasService.buscarAbastecimentos(
                request.dataInicial(),
                request.dataFinal()
        );
        System.out.println("Retornando: " + resultado.size() + " registros");
        return ResponseEntity.ok(resultado);
    }
}

