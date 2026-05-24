package com.jardel.LogiDash.database.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "abastecimento_itens")
public class AbastecimentoItemEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "abastecimento_id", nullable = false)
    private AbastecimentoEntity abastecimento;

    @Column(nullable = false, name = "tipo_combustivel")
    private String tipoCombustivel;

    @Column(name = "quantidade", precision = 10, scale = 4, nullable = false)
    private BigDecimal quantidade;

    @Column(name = "valor_unitario", precision = 10, scale = 4)
    private BigDecimal valorUnitario;

    @Column(name = "valor_total", precision = 10, scale = 4)
    private BigDecimal valorTotal;
}
