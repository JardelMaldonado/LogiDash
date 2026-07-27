package com.jardel.LogiDash.integration;

import com.jardel.LogiDash.database.model.AbastecimentoEntity;
import com.jardel.LogiDash.database.model.AbastecimentoItemEntity;
import com.jardel.LogiDash.database.repository.IAbastecimentoRepository;
import com.jardel.LogiDash.scheduler.ImportacaoScheduler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
@Import(WireMockConfig.class)
public class AbastecimentoRepositoryIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private IAbastecimentoRepository repository;


    @MockitoBean
    private ImportacaoScheduler importacaoScheduler;

    @AfterEach
    void limparBanco() {
        repository.deleteAll();
    }

    @Test
    void quandoBuscaPorDataComItens_deveCarregarItensSemLazyException() {

        AbastecimentoItemEntity item1 = AbastecimentoItemEntity.builder()
                .tipoCombustivel("Diesel S10")
                .quantidade(new BigDecimal("50.0000"))
                .valorUnitario(new BigDecimal("5.8900"))
                .valorTotal(new BigDecimal("294.5000"))
                .build();

        AbastecimentoItemEntity item2 = AbastecimentoItemEntity.builder()
                .tipoCombustivel("Arla 32")
                .quantidade(new BigDecimal("10.0000"))
                .valorUnitario(null)
                .valorTotal(null)
                .build();

        AbastecimentoEntity abastecimento = AbastecimentoEntity.builder()
                .identificador(100L)
                .data(LocalDateTime.of(2026, 7, 15, 10, 0))
                .placa("ABC1234")
                .nomeMotorista("João Silva")
                .razaoSocialPosto("Posto Central")
                .postoInterno(true)
                .itens(List.of(item1, item2))
                .build();

        item1.setAbastecimento(abastecimento);
        item2.setAbastecimento(abastecimento);

        repository.save(abastecimento);

        List<AbastecimentoEntity> resultado = repository.findByDataBetweenWithItens(
                LocalDateTime.of(2026, 7, 1, 0, 0),
                LocalDateTime.of(2026, 7, 31, 23, 59)
        );

        assertThat(resultado).hasSize(1);
        AbastecimentoEntity encontrado = resultado.getFirst();
        assertThat(encontrado.getData()).isEqualTo(LocalDateTime.of(2026, 7, 15, 10, 0));

        assertThat(encontrado.getItens()).hasSize(2);
        assertThat(encontrado.getItens())
                .extracting(AbastecimentoItemEntity::getTipoCombustivel)
                .containsExactlyInAnyOrder("Diesel S10", "Arla 32");
    }

    @Test
    void quandoBuscaPorData_deveFiltrarCorretamentePeloIntervaloDeDatas() {

        AbastecimentoItemEntity item1 = AbastecimentoItemEntity.builder()
                .tipoCombustivel("Diesel S10")
                .quantidade(new BigDecimal("50.0000"))
                .valorUnitario(new BigDecimal("5.8900"))
                .valorTotal(new BigDecimal("294.5000"))
                .build();

        AbastecimentoItemEntity item2 = AbastecimentoItemEntity.builder()
                .tipoCombustivel("Arla 32")
                .quantidade(new BigDecimal("10.0000"))
                .valorUnitario(null)
                .valorTotal(null)
                .build();

        AbastecimentoEntity abastecimento = AbastecimentoEntity.builder()
                .identificador(101L)
                .data(LocalDateTime.of(2026, 7, 15, 10, 0))
                .placa("ABC1234")
                .nomeMotorista("João Silva")
                .razaoSocialPosto("Posto Central")
                .postoInterno(true)
                .itens(List.of(item1, item2))
                .build();

        AbastecimentoItemEntity item3 = AbastecimentoItemEntity.builder()
                .tipoCombustivel("Diesel S10")
                .quantidade(new BigDecimal("50.0000"))
                .valorUnitario(new BigDecimal("5.8900"))
                .valorTotal(new BigDecimal("294.5000"))
                .build();

        AbastecimentoItemEntity item4 = AbastecimentoItemEntity.builder()
                .tipoCombustivel("Arla 32")
                .quantidade(new BigDecimal("10.0000"))
                .valorUnitario(null)
                .valorTotal(null)
                .build();

        AbastecimentoEntity abastecimento1 = AbastecimentoEntity.builder()
                .identificador(102L)
                .data(LocalDateTime.of(2026, 5, 15, 10, 0))
                .placa("ABC1234")
                .nomeMotorista("João Silva")
                .razaoSocialPosto("Posto Central")
                .postoInterno(true)
                .itens(List.of(item3, item4))
                .build();


        item1.setAbastecimento(abastecimento);
        item2.setAbastecimento(abastecimento);
        item3.setAbastecimento(abastecimento1);
        item4.setAbastecimento(abastecimento1);

        repository.saveAll(List.of(abastecimento, abastecimento1));

        List<AbastecimentoEntity> resultado = repository.findByDataBetweenWithItens(
                LocalDateTime.of(2026, 7, 1, 0, 0),
                LocalDateTime.of(2026, 7, 31, 23, 59)
        );


        assertThat(resultado).hasSize(1);
        AbastecimentoEntity encontrado = resultado.getFirst();
        assertThat(encontrado.getData()).isEqualTo(LocalDateTime.of(2026, 7, 15, 10, 0));
        assertThat(encontrado.getItens()).hasSize(2);
        assertThat(encontrado.getItens())
                .extracting(AbastecimentoItemEntity::getTipoCombustivel)
                .containsExactlyInAnyOrder("Diesel S10", "Arla 32");
    }
    @Test
    void quandoIdentificadorExiste_deveRetornarTrue() {
        AbastecimentoEntity abastecimento = AbastecimentoEntity.builder()
                .identificador(101L)
                .data(LocalDateTime.of(2026, 7, 15, 10, 0))
                .placa("ABC1234")
                .nomeMotorista("João Silva")
                .razaoSocialPosto("Posto Central")
                .postoInterno(true)
                .build();

        repository.save(abastecimento);

        assertThat(repository.existsByIdentificador(101L)).isTrue();
    }

    @Test
    void quandoIdentificadorNaoExiste_deveRetornarFalse() {
        assertThat(repository.existsByIdentificador(999L)).isFalse();
    }

}
