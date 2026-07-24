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
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "abastecimento_itens_seq")
    @SequenceGenerator(name = "abastecimento_itens_seq", sequenceName = "abastecimento_itens_seq", allocationSize = 1)
    private Integer id;

    @Column(nullable = false,  unique = true)
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

}
