package com.jardel.LogiDash.controller;

import com.jardel.LogiDash.dto.dashboard.DashboardResponse;
import com.jardel.LogiDash.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard(
            @RequestParam String dataInicial,
            @RequestParam String dataFinal,
            @RequestParam(required = false) String placa,
            @RequestParam(required = false) String motorista) {
        return ResponseEntity.ok(dashboardService.calcular(dataInicial, dataFinal, placa, motorista));
    }
}
