package com.jardel.LogiDash.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jardel.LogiDash.dto.ItemAbastecimentoDTO;
import com.jardel.LogiDash.dto.MotoristaDTO;
import com.jardel.LogiDash.dto.PontoVendaDTO;
import com.jardel.LogiDash.dto.VeiculoDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AbastecimentoResponse{

    private Long identificador;
    private String data;
    private String motivoRecusa;
    private Integer abastecimentoEstornado;
    private VeiculoDTO veiculo;
    private MotoristaDTO motorista;
    private PontoVendaDTO pontoVenda;

    @JsonProperty("items")
    private List<ItemAbastecimentoDTO> itensLista;

    @JsonProperty("valorTotalCalculado")
    public BigDecimal getValorTotalCalculado() {
        if (itensLista == null) return BigDecimal.ZERO;
        return itensLista.stream()
                .map(item -> item.valorTotal() != null ? item.valorTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @JsonProperty("litros")
    public BigDecimal getLitros() {
        if (itensLista == null) return BigDecimal.ZERO;
        return itensLista.stream()
                .map(item -> item.quantidade() != null ? item.quantidade() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public String getPlaca() { return veiculo != null ? veiculo.placa() : "N/A"; }
    public String getNomeMotorista() { return motorista != null ? motorista.nome() : "N/A"; }
}
