package com.jardel.LogiDash.database.repository;

import com.jardel.LogiDash.database.model.AbastecimentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface IAbastecimentoRepository extends JpaRepository<AbastecimentoEntity, Integer> {
    boolean existsByIdentificador(Long identificador);
    List<AbastecimentoEntity> findByDataBetween(LocalDateTime inicio, LocalDateTime fim);
}
