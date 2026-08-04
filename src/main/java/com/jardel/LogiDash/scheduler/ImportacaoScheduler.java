package com.jardel.LogiDash.scheduler;

import com.jardel.LogiDash.service.AbastecimentoService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class ImportacaoScheduler implements ApplicationRunner {

    private final AbastecimentoService abastecimentoService;

    @Override
    public void run(@NonNull ApplicationArguments args) {
        String dataFim = LocalDate.now().toString();
        String dataInicio = LocalDate.now().minusDays(3).toString();
        abastecimentoService.importarAbastecimentos(dataInicio, dataFim);
    }

    @Scheduled(cron = "0 0 11 * * *")
    public void importarDiario() {
        String dataFim = LocalDate.now().toString();
        String dataInicio = LocalDate.now().minusDays(3).toString();
        abastecimentoService.importarAbastecimentos(dataInicio, dataFim);
    }

    @Scheduled(cron = "0 0 11 1 * *")
    public void importarMesAnterior() {
        LocalDate primeiroDiaMesAnterior = LocalDate.now().minusMonths(1).withDayOfMonth(1);
        LocalDate ultimoDiaMesAnterior = LocalDate.now().withDayOfMonth(1).minusDays(1);
        abastecimentoService.importarAbastecimentos(
                primeiroDiaMesAnterior.toString(),
                ultimoDiaMesAnterior.toString()
        );
    }
}
