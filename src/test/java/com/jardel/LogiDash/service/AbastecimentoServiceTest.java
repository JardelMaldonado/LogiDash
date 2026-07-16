package com.jardel.LogiDash.service;

import com.jardel.LogiDash.database.model.AbastecimentoEntity;
import com.jardel.LogiDash.database.model.AbastecimentoItemEntity;
import com.jardel.LogiDash.database.repository.IAbastecimentoRepository;
import com.jardel.LogiDash.dto.abastecimento.Abastecimento;
import com.jardel.LogiDash.dto.abastecimento.AbastecimentoResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AbastecimentoServiceTest {

    @Mock
    private ProFrotasService proFrotasService;

    @Mock
    private IAbastecimentoRepository abastecimentoRepository;

    @InjectMocks
    private AbastecimentoService abastecimentoService;

    @Test
    @DisplayName("Nao deve salvar abastecimento duplicado")
    void quandoIdentificadorJaExistir_naoDeveSalvarAbastecimento() {

        AbastecimentoResponse abastecimento = new AbastecimentoResponse();
        abastecimento.setIdentificador(12345L);
        abastecimento.setData("2024-01-15 10:30:00.000");
        abastecimento.setMotivoRecusa(null);
        abastecimento.setAbastecimentoEstornado(0);
        abastecimento.setMotorista(null);
        abastecimento.setVeiculo(null);

        when(proFrotasService.buscarAbastecimentos("2024-01-01", "2024-01-31")).thenReturn(List.of(abastecimento));
        when(abastecimentoRepository.existsByIdentificador(12345L)).thenReturn(true);

        abastecimentoService.importarAbastecimentos("2024-01-01", "2024-01-31");

        verify(abastecimentoRepository).existsByIdentificador(12345L);
        verify(abastecimentoRepository, never()).save(any());
    }

    @Test
    @DisplayName("deve salvar abastecimento corretamente")
    void quandoIdentificadorNaoExistir_deveSalvarAbastecimento() {

        AbastecimentoResponse abastecimento = new AbastecimentoResponse();
        abastecimento.setIdentificador(12345L);
        abastecimento.setData("2024-01-15 10:30:00.000");
        abastecimento.setMotivoRecusa(null);
        abastecimento.setAbastecimentoEstornado(0);
        abastecimento.setMotorista(null);
        abastecimento.setVeiculo(null);

        when(proFrotasService.buscarAbastecimentos("2024-01-01", "2024-01-31")).thenReturn(List.of(abastecimento));
        when(abastecimentoRepository.existsByIdentificador(12345L)).thenReturn(false);
        when(abastecimentoRepository.save(any())).thenReturn(new AbastecimentoEntity());

        abastecimentoService.importarAbastecimentos("2024-01-01", "2024-01-31");

        verify(abastecimentoRepository).existsByIdentificador(12345L);

        ArgumentCaptor<AbastecimentoEntity> captor = ArgumentCaptor.forClass(AbastecimentoEntity.class);
        verify(abastecimentoRepository).save(captor.capture());

        AbastecimentoEntity salvo = captor.getValue();
        assertThat(salvo.getIdentificador()).isEqualTo(12345L);
    }

    @Test
    @DisplayName("deve converter entity para DTO corretamente")
    void quandoBuscarAbastecimentos_deveConverterEntityParaDto() {

        AbastecimentoItemEntity item = new AbastecimentoItemEntity();
        item.setTipoCombustivel("Diesel");
        item.setQuantidade(new BigDecimal("400"));
        item.setValorUnitario(new BigDecimal("6.11"));
        item.setValorTotal(new BigDecimal("2444.00"));

        AbastecimentoEntity abastecimento = new AbastecimentoEntity();
        abastecimento.setPlaca("RMQ1C72");
        abastecimento.setNomeMotorista("João");
        abastecimento.setPostoInterno(false);
        abastecimento.setRazaoSocialPosto("Posto Externo SA");
        abastecimento.setData(LocalDateTime.of(2024, 1, 10, 8, 0));
        abastecimento.setItens(List.of(item));

        when(abastecimentoRepository.findByDataBetweenWithItens(any(), any())).thenReturn(List.of(abastecimento));

        List<Abastecimento> resultado = abastecimentoService.buscarAbastecimentos("2024-01-01", "2024-01-31");

        assertThat(resultado.getFirst().placa()).isEqualTo("RMQ1C72");
        assertThat(resultado.getFirst().nomeMotorista()).isEqualTo("João");
        assertThat(resultado.getFirst().postoInterno()).isFalse();
        var itens = resultado.getFirst().itens();
        assertThat(itens.getFirst().tipoCombustivel()).isEqualTo("Diesel");

        assertThat(itens.getFirst().quantidade()).isEqualByComparingTo(new BigDecimal("400"));
        assertThat(itens.getFirst().valorUnitario()).isEqualByComparingTo(new BigDecimal("6.11"));
        assertThat(itens.getFirst().valorTotal()).isEqualByComparingTo(new BigDecimal("2444.00"));
    }
}