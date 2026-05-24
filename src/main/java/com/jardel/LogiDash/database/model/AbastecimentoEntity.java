package com.jardel.LogiDash.database.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "abastecimentos")
public class AbastecimentoEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer id;

    @Column(nullable = false)
    private Long identificador;

    @Column(nullable = false)
    private LocalDateTime data;

    @Column(nullable = false)
    private String placa;

    @Column(nullable = false, name = "nome_motorista")
    private String nomeMotorista;

    @Column(nullable = false, name = "razao_social_posto")
    private String razaoSocialPosto;

    @Column(nullable = false, name = "posto_interno")
    private boolean postoInterno;

    @OneToMany(mappedBy = "abastecimento", cascade = CascadeType.ALL)
    private List<AbastecimentoItemEntity> itens;

    public BigDecimal getTotalLitros() {
        if (itens == null) return BigDecimal.ZERO;
        return itens.stream()
                .map(i -> i.getQuantidade() != null ? i.getQuantidade() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getValorTotalCalculado() {
        if (itens == null) return BigDecimal.ZERO;
        return itens.stream()
                .map(i -> i.getValorTotal() != null ? i.getValorTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
