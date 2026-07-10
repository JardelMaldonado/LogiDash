package com.jardel.LogiDash.service;

import com.jardel.LogiDash.dto.dashboard.DashboardResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.jardel.LogiDash.database.model.AbastecimentoEntity;
import com.jardel.LogiDash.database.model.AbastecimentoItemEntity;
import java.time.LocalDateTime;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private AbastecimentoService abastecimentoService;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    @DisplayName("Quando não há abastecimentos, todos os totais devem ser zero")
    void quandoListaVazia_devRetornarTotaisZerados() {


        when(abastecimentoService.buscarEntities("2024-01-01", "2024-01-31"))
                .thenReturn(List.of());


        DashboardResponse response = dashboardService.calcular(
                "2024-01-01", "2024-01-31", null, null
        );


        assertThat(response.totalGeral())
                .isEqualByComparingTo(BigDecimal.ZERO);

        assertThat(response.totalLitros())
                .isEqualByComparingTo(BigDecimal.ZERO);

        assertThat(response.totalAbastecimentos())
                .isZero();

        assertThat(response.rankingPostos())
                .isEmpty();

        assertThat(response.rankingMotoristas())
                .isEmpty();

        assertThat(response.gastoDiario())
                .isEmpty();

        assertThat(response.todasPlacas())
                .isEmpty();

        assertThat(response.todosMotoristas())
                .isEmpty();
    }
    @Test
    @DisplayName("Deve calcular total geral corretamente com um abastecimento")
    void deveCalcularTotalGeralComUmAbastecimento() {

        AbastecimentoItemEntity item = new AbastecimentoItemEntity();
        item.setTipoCombustivel("Diesel");
        item.setQuantidade(new BigDecimal("100"));
        item.setValorUnitario(new BigDecimal("6.00"));
        item.setValorTotal(new BigDecimal("600.00"));


        AbastecimentoEntity abastecimento = new AbastecimentoEntity();
        abastecimento.setPlaca("ABC1234");
        abastecimento.setNomeMotorista("João");
        abastecimento.setPostoInterno(false);
        abastecimento.setRazaoSocialPosto("Posto Externo SA");
        abastecimento.setData(LocalDateTime.of(2024, 1, 10, 8, 0));
        abastecimento.setItens(List.of(item));


        when(abastecimentoService.buscarEntities("2024-01-01", "2024-01-31"))
                .thenReturn(List.of(abastecimento));


        DashboardResponse response = dashboardService.calcular(
                "2024-01-01", "2024-01-31", null, null
        );


        assertThat(response.totalAbastecimentos()).isEqualTo(1);
        assertThat(response.totalLitros()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(response.totalGeral()).isEqualByComparingTo(new BigDecimal("600.00"));
    }
    @Test
    @DisplayName("Deve filtrar abastecimentos por placa corretamente")
    void deveFiltrarPorPlacaCorretamente() {

        AbastecimentoItemEntity item = new AbastecimentoItemEntity();
        item.setTipoCombustivel("Diesel");
        item.setQuantidade(new BigDecimal("400"));
        item.setValorUnitario(new BigDecimal("6.11"));
        item.setValorTotal(new BigDecimal("2444.00"));

        AbastecimentoItemEntity item2 = new AbastecimentoItemEntity();
        item2.setTipoCombustivel("Diesel");
        item2.setQuantidade(new BigDecimal("500"));
        item2.setValorUnitario(new BigDecimal("6.11"));
        item2.setValorTotal(new BigDecimal("3055.00"));

        AbastecimentoEntity abastecimento = new AbastecimentoEntity();
        abastecimento.setPlaca("ABC1234");
        abastecimento.setNomeMotorista("João");
        abastecimento.setPostoInterno(false);
        abastecimento.setRazaoSocialPosto("Posto Externo SA");
        abastecimento.setData(LocalDateTime.of(2024, 1, 10, 8, 0));
        abastecimento.setItens(List.of(item));

        AbastecimentoEntity abastecimento2 = new AbastecimentoEntity();
        abastecimento2.setPlaca("XYZ5678");
        abastecimento2.setNomeMotorista("Maria");
        abastecimento2.setPostoInterno(false);
        abastecimento2.setRazaoSocialPosto("Posto Externo SA");
        abastecimento2.setData(LocalDateTime.of(2024, 1, 11, 8, 0));
        abastecimento2.setItens(List.of(item2));

        when(abastecimentoService.buscarEntities("2024-01-01", "2024-01-31"))
                .thenReturn(List.of(abastecimento, abastecimento2));


        DashboardResponse response = dashboardService.calcular(
                "2024-01-01", "2024-01-31", "ABC1234", null
        );

        assertThat(response.totalAbastecimentos()).isEqualTo(1);
        assertThat(response.totalLitros()).isEqualByComparingTo(new BigDecimal("400.00"));
    }
    @Test
    @DisplayName("todasPlacas() deve retornar todas as placas independente do filtro aplicado")
    void deveRetornarTodasAsPlacasIndependenteDoFiltro() {

        AbastecimentoItemEntity item = new AbastecimentoItemEntity();
        item.setTipoCombustivel("Diesel");
        item.setQuantidade(new BigDecimal("400"));
        item.setValorUnitario(new BigDecimal("6.11"));
        item.setValorTotal(new BigDecimal("2444.00"));

        AbastecimentoItemEntity item2 = new AbastecimentoItemEntity();
        item2.setTipoCombustivel("Diesel");
        item2.setQuantidade(new BigDecimal("500"));
        item2.setValorUnitario(new BigDecimal("6.11"));
        item2.setValorTotal(new BigDecimal("3055.00"));

        AbastecimentoEntity abastecimento = new AbastecimentoEntity();
        abastecimento.setPlaca("ABC1234");
        abastecimento.setNomeMotorista("João");
        abastecimento.setPostoInterno(false);
        abastecimento.setRazaoSocialPosto("Posto Externo SA");
        abastecimento.setData(LocalDateTime.of(2024, 1, 10, 8, 0));
        abastecimento.setItens(List.of(item));

        AbastecimentoEntity abastecimento2 = new AbastecimentoEntity();
        abastecimento2.setPlaca("XYZ5678");
        abastecimento2.setNomeMotorista("Maria");
        abastecimento2.setPostoInterno(false);
        abastecimento2.setRazaoSocialPosto("Posto Externo SA");
        abastecimento2.setData(LocalDateTime.of(2024, 1, 11, 8, 0));
        abastecimento2.setItens(List.of(item2));

        when(abastecimentoService.buscarEntities("2024-01-01", "2024-01-31"))
                .thenReturn(List.of(abastecimento, abastecimento2));

        DashboardResponse response = dashboardService.calcular(
                "2024-01-01", "2024-01-31", "ABC1234", null
        );

        assertThat(response.todasPlacas()).containsExactly("ABC1234", "XYZ5678");
    }
    @Test
    @DisplayName("Deve retornar as somas das quantidades dos itens corretamente")
    void deveRetornarAsSomasCorretamente() {

        AbastecimentoItemEntity item = new AbastecimentoItemEntity();
        item.setTipoCombustivel("Diesel");
        item.setQuantidade(new BigDecimal("800"));
        item.setValorUnitario(new BigDecimal("6.11"));
        item.setValorTotal(new BigDecimal("4888.00"));

        AbastecimentoItemEntity itemDiesel2 = new AbastecimentoItemEntity();
        itemDiesel2.setTipoCombustivel("Diesel");
        itemDiesel2.setQuantidade(new BigDecimal("500"));
        itemDiesel2.setValorUnitario(new BigDecimal("6.11"));
        itemDiesel2.setValorTotal(new BigDecimal("3055.00"));

        AbastecimentoItemEntity itemArlaGranel = new AbastecimentoItemEntity();
        itemArlaGranel.setTipoCombustivel("arla 32 - granel");
        itemArlaGranel.setQuantidade(new BigDecimal("50"));
        itemArlaGranel.setValorUnitario(new BigDecimal("3.49"));
        itemArlaGranel.setValorTotal(new BigDecimal("174.50"));

        AbastecimentoEntity abastecimento = new AbastecimentoEntity();
        abastecimento.setPlaca("QOX2B71");
        abastecimento.setNomeMotorista("Jardel");
        abastecimento.setPostoInterno(false);
        abastecimento.setRazaoSocialPosto("Posto Externo");
        abastecimento.setData(LocalDateTime.of(2024, 1, 15, 8, 0));
        abastecimento.setItens(List.of(item));

        AbastecimentoEntity abastecimento2 = new AbastecimentoEntity();
        abastecimento2.setPlaca("RMN0D01");
        abastecimento2.setNomeMotorista("Maria");
        abastecimento2.setPostoInterno(false);
        abastecimento2.setRazaoSocialPosto("Posto Externo");
        abastecimento2.setData(LocalDateTime.of(2024, 1, 11, 8, 0));
        abastecimento2.setItens(List.of(itemDiesel2, itemArlaGranel));


        when(abastecimentoService.buscarEntities("2024-01-01", "2024-01-31"))
                .thenReturn(List.of(abastecimento, abastecimento2));


        DashboardResponse response = dashboardService.calcular(
                "2024-01-01", "2024-01-31", null, null
        );


        assertThat(response.totalAbastecimentos()).isEqualTo(2);
        assertThat(response.consumo().dieselExterno()).isEqualByComparingTo(new BigDecimal("1300.00"));
        assertThat(response.consumo().arlaGranelExterno()).isEqualByComparingTo(new BigDecimal("50.00"));
    }
    @Test
    @DisplayName("Arla Balde com quantidade maior que 5 deve ser somado no granel externo")
    void deveClassificarArlaBaldeSuspeitoComoGranel() {


        AbastecimentoItemEntity itemArlaBalde = new AbastecimentoItemEntity();
        itemArlaBalde.setTipoCombustivel("arla 32 - balde");
        itemArlaBalde.setQuantidade(new BigDecimal("50.00"));
        itemArlaBalde.setValorUnitario(new BigDecimal("3.49"));
        itemArlaBalde.setValorTotal(new BigDecimal("174.50"));

        AbastecimentoEntity abastecimento = new AbastecimentoEntity();
        abastecimento.setPlaca("QOX2B71");
        abastecimento.setNomeMotorista("Jardel");
        abastecimento.setPostoInterno(false);
        abastecimento.setRazaoSocialPosto("Posto Externo");
        abastecimento.setData(LocalDateTime.of(2024, 1, 4, 8, 0));
        abastecimento.setItens(List.of(itemArlaBalde));


        when(abastecimentoService.buscarEntities("2024-01-01", "2024-01-31"))
                .thenReturn(List.of(abastecimento));


        DashboardResponse response = dashboardService.calcular(
                "2024-01-01", "2024-01-31", null, null
        );


        assertThat(response.consumo().arlaGranelExterno()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(response.consumo().arlaBaldeExterno()).isEqualByComparingTo(BigDecimal.ZERO);
    }
    @Test
    @DisplayName("Arla Balde com quantidade menor ou igual a 5 deve ser convertido para litros (x20)")
    void deveConverterArlaBaldeRealParaLitros() {

        AbastecimentoItemEntity itemArlaBalde = new AbastecimentoItemEntity();
        itemArlaBalde.setTipoCombustivel("arla 32 - balde");
        itemArlaBalde.setQuantidade(new BigDecimal("3.00"));
        itemArlaBalde.setValorUnitario(new BigDecimal("75.00"));
        itemArlaBalde.setValorTotal(new BigDecimal("225.00"));

        AbastecimentoEntity abastecimento = new AbastecimentoEntity();
        abastecimento.setPlaca("QOX2B71");
        abastecimento.setNomeMotorista("Jardel");
        abastecimento.setPostoInterno(false);
        abastecimento.setRazaoSocialPosto("Posto Externo");
        abastecimento.setData(LocalDateTime.of(2024, 1, 4, 8, 0));
        abastecimento.setItens(List.of(itemArlaBalde));

        when(abastecimentoService.buscarEntities("2024-01-01", "2024-01-31"))
                .thenReturn(List.of(abastecimento));


        DashboardResponse response = dashboardService.calcular(
                "2024-01-01", "2024-01-31", null, null
        );

        assertThat(response.consumo().arlaBaldeExterno()).isEqualByComparingTo(new BigDecimal("60.00"));
    }
    @Test
    @DisplayName("Deve retornar os motoristas corretamente")
    void deveRetornarOsMotoristasCorretamente() {

        AbastecimentoItemEntity itemDiesel = new AbastecimentoItemEntity();
        itemDiesel.setTipoCombustivel("Diesel");
        itemDiesel.setQuantidade(new BigDecimal("400"));
        itemDiesel.setValorUnitario(new BigDecimal("6.11"));
        itemDiesel.setValorTotal(new BigDecimal("2444.00"));

        AbastecimentoItemEntity itemDiesel2 = new AbastecimentoItemEntity();
        itemDiesel2.setTipoCombustivel("Diesel");
        itemDiesel2.setQuantidade(new BigDecimal("500"));
        itemDiesel2.setValorUnitario(new BigDecimal("6.11"));
        itemDiesel2.setValorTotal(new BigDecimal("3055.00"));

        AbastecimentoEntity abastecimento = new AbastecimentoEntity();
        abastecimento.setPlaca("RMN0D02");
        abastecimento.setNomeMotorista("Jardel");
        abastecimento.setPostoInterno(false);
        abastecimento.setRazaoSocialPosto("Posto Externo SA");
        abastecimento.setData(LocalDateTime.of(2024, 1, 10, 8, 0));
        abastecimento.setItens(List.of(itemDiesel));

        AbastecimentoEntity abastecimento2 = new AbastecimentoEntity();
        abastecimento2.setPlaca("RMN0D01");
        abastecimento2.setNomeMotorista("Luiz");
        abastecimento2.setPostoInterno(false);
        abastecimento2.setRazaoSocialPosto("Posto Externo SA");
        abastecimento2.setData(LocalDateTime.of(2024, 1, 11, 8, 0));
        abastecimento2.setItens(List.of(itemDiesel2));

        when(abastecimentoService.buscarEntities("2024-01-01", "2024-01-31"))
                .thenReturn(List.of(abastecimento, abastecimento2));


        DashboardResponse response = dashboardService.calcular(
                "2024-01-01", "2024-01-31", null, "Jardel"
        );


        assertThat(response.totalAbastecimentos()).isEqualTo(1);
        assertThat(response.totalLitros()).isEqualByComparingTo(new BigDecimal("400.00"));
        assertThat(response.todosMotoristas()).containsExactly("Jardel", "Luiz");
    }
    @Test
    @DisplayName("Deve retornar os postos com maiores consumos corretamente")
    void deveRetornarOsPostosComMaioresConsumosCorretamente() {

        AbastecimentoItemEntity itemDiesel = new AbastecimentoItemEntity();
        itemDiesel.setTipoCombustivel("Diesel");
        itemDiesel.setQuantidade(new BigDecimal("800"));
        itemDiesel.setValorUnitario(new BigDecimal("6.11"));
        itemDiesel.setValorTotal(new BigDecimal("4888.00"));

        AbastecimentoItemEntity itemDiesel2 = new AbastecimentoItemEntity();
        itemDiesel2.setTipoCombustivel("Diesel");
        itemDiesel2.setQuantidade(new BigDecimal("500"));
        itemDiesel2.setValorUnitario(new BigDecimal("6.11"));
        itemDiesel2.setValorTotal(new BigDecimal("3055.00"));


        AbastecimentoEntity abastecimento = new AbastecimentoEntity();
        abastecimento.setPlaca("QOX2B71");
        abastecimento.setNomeMotorista("Jardel");
        abastecimento.setPostoInterno(false);
        abastecimento.setRazaoSocialPosto("Posto Beija Flor");
        abastecimento.setData(LocalDateTime.of(2024, 1, 15, 8, 0));
        abastecimento.setItens(List.of(itemDiesel));

        AbastecimentoEntity abastecimento2 = new AbastecimentoEntity();
        abastecimento2.setPlaca("RMN0D01");
        abastecimento2.setNomeMotorista("Maria");
        abastecimento2.setPostoInterno(false);
        abastecimento2.setRazaoSocialPosto("Posto Externo SA");
        abastecimento2.setData(LocalDateTime.of(2024, 1, 11, 8, 0));
        abastecimento2.setItens(List.of(itemDiesel2));


        when(abastecimentoService.buscarEntities("2024-01-01", "2024-01-31"))
                .thenReturn(List.of(abastecimento, abastecimento2));


        DashboardResponse response = dashboardService.calcular(
                "2024-01-01", "2024-01-31", null, null
        );


        assertThat(response.totalAbastecimentos()).isEqualTo(2);
        assertThat(response.rankingPostos().get(0).nome()).isEqualTo("Posto Beija Flor");
        assertThat(response.rankingPostos().get(1).nome()).isEqualTo("Posto Externo SA");
    }
    @Test
    @DisplayName("Deve retornar os gastos diario corretamente")
    void deveRetornarOsGastosDiariosCorretamente() {

        AbastecimentoItemEntity item = new AbastecimentoItemEntity();
        item.setTipoCombustivel("Diesel");
        item.setQuantidade(new BigDecimal("400"));
        item.setValorUnitario(new BigDecimal("6.11"));
        item.setValorTotal(new BigDecimal("2444.00"));

        AbastecimentoItemEntity item2 = new AbastecimentoItemEntity();
        item2.setTipoCombustivel("Diesel");
        item2.setQuantidade(new BigDecimal("500"));
        item2.setValorUnitario(new BigDecimal("6.89"));
        item2.setValorTotal(new BigDecimal("3445.00"));

        AbastecimentoEntity abastecimento = new AbastecimentoEntity();
        abastecimento.setPlaca("RMQ1C72");
        abastecimento.setNomeMotorista("João");
        abastecimento.setPostoInterno(false);
        abastecimento.setRazaoSocialPosto("Posto Externo SA");
        abastecimento.setData(LocalDateTime.of(2024, 1, 10, 8, 0));
        abastecimento.setItens(List.of(item));

        AbastecimentoEntity abastecimento2 = new AbastecimentoEntity();
        abastecimento2.setPlaca("RMQ1C13");
        abastecimento2.setNomeMotorista("Maria");
        abastecimento2.setPostoInterno(false);
        abastecimento2.setRazaoSocialPosto("Posto Externo");
        abastecimento2.setData(LocalDateTime.of(2024, 1, 10, 9, 0));
        abastecimento2.setItens(List.of(item2));

        when(abastecimentoService.buscarEntities("2024-01-01", "2024-01-31"))
                .thenReturn(List.of(abastecimento, abastecimento2));


        DashboardResponse response = dashboardService.calcular(
                "2024-01-01", "2024-01-31", null, null
        );


        assertThat(response.totalAbastecimentos()).isEqualTo(2);
        assertThat(response.gastoDiario()).hasSize(1);
        assertThat(response.gastoDiario().get(0).dia()).isEqualTo("2024-01-10");
        assertThat(response.gastoDiario().get(0).valor()).isEqualByComparingTo(new BigDecimal("5889.00"));
        assertThat(response.gastoDiario().get(0).litros()).isEqualByComparingTo(new BigDecimal("900.00"));
    }
}