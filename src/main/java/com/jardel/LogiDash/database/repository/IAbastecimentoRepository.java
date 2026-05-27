package com.jardel.LogiDash.database.repository;

import com.jardel.LogiDash.database.model.AbastecimentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface IAbastecimentoRepository extends JpaRepository<AbastecimentoEntity, Integer> {
    boolean existsByIdentificador(Long identificador);
    @Query("SELECT a FROM AbastecimentoEntity a JOIN FETCH a.itens WHERE a.data BETWEEN :inicio AND :fim")
    List<AbastecimentoEntity> findByDataBetweenWithItens(
            @Param("inicio")LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );
}
