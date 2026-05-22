package com.jardel.LogiDash.dto.abastecimento;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    private Veiculo veiculo;
    private Motorista motorista;
    private PontoVenda pontoVenda;

    @JsonProperty("items")
    private List<ItemAbastecimento> itensLista;
    
    public String getPlaca() { return veiculo != null ? veiculo.placa() : "N/A"; }
    public String getNomeMotorista() { return motorista != null ? motorista.nome() : "N/A"; }
}
