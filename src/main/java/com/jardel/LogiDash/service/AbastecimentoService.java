package com.jardel.LogiDash.service;


import com.jardel.LogiDash.database.model.AbastecimentoEntity;
import com.jardel.LogiDash.database.model.AbastecimentoItemEntity;
import com.jardel.LogiDash.database.repository.IAbastecimentoRepository;
import com.jardel.LogiDash.dto.abastecimento.Abastecimento;
import com.jardel.LogiDash.dto.abastecimento.AbastecimentoItem;
import com.jardel.LogiDash.dto.abastecimento.AbastecimentoResponse;
import com.jardel.LogiDash.dto.abastecimento.ItemAbastecimento;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AbastecimentoService {
    private final ProFrotasService proFrotasService;
    private final IAbastecimentoRepository  abastecimentoRepository;

    @Transactional
    public void importarAbastecimentos(String dataInicio, String dataFim ) {
        List<AbastecimentoResponse> abastecimentos = proFrotasService.buscarAbastecimentos(dataInicio, dataFim);
        abastecimentos.forEach(this::salvarSeNaoExistir);
    }

    private void salvarSeNaoExistir(AbastecimentoResponse response) {
        if(abastecimentoRepository.existsByIdentificador(response.getIdentificador())) {
            return;
        }
        AbastecimentoEntity entity = toEntity(response);
        abastecimentoRepository.save(entity);
    }

    private AbastecimentoEntity toEntity(AbastecimentoResponse response) {
        AbastecimentoEntity entity = AbastecimentoEntity.builder()
                .identificador(response.getIdentificador())
                .data(LocalDateTime.parse(response.getData(),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")))
                .placa(response.getPlaca())
                .nomeMotorista(response.getNomeMotorista())
                .razaoSocialPosto(response.getPontoVenda() != null ? response.getPontoVenda().razaoSocial() : null)
                .postoInterno(response.getPontoVenda() != null && response.getPontoVenda().postoInterno())
                .build();

        List<AbastecimentoItemEntity> itens = toItensEntity(response.getItensLista());
        itens.forEach(item -> item.setAbastecimento(entity)); // vincula o pai
        entity.setItens(itens);
        return entity;
    }

    private List<AbastecimentoItemEntity> toItensEntity(List<ItemAbastecimento> itens) {
        if(itens == null) return List.of();
        return itens.stream()
                .map(item -> AbastecimentoItemEntity.builder()
                        .tipoCombustivel(item.nome())
                        .quantidade(item.quantidade())
                        .valorUnitario(item.valorUnitario())
                        .valorTotal(item.valorTotal())
                        .build())
                .toList();
    }

    public List<AbastecimentoEntity> buscarEntities(String dataInicio, String dataFim) {
        LocalDateTime inicio = LocalDateTime.parse(dataInicio + "T00:00:00");
        LocalDateTime fim = LocalDateTime.parse(dataFim + "T23:59:59");
        return abastecimentoRepository.findByDataBetweenWithItens(inicio, fim);
    }
    public List<Abastecimento> buscarAbastecimentos(String dataInicio, String dataFim) {
        List<AbastecimentoEntity> entities = buscarEntities(dataInicio, dataFim);
        return toAbastecimento(entities);
    }

    private List<Abastecimento> toAbastecimento(List<AbastecimentoEntity> entities) {
        return entities.stream()
                .map(e -> new Abastecimento(
                        e.getIdentificador(),
                        e.getData(),
                        e.getPlaca(),
                        e.getNomeMotorista(),
                        e.getRazaoSocialPosto(),
                        e.isPostoInterno(),
                        e.getItens().stream()
                                .map(i -> new AbastecimentoItem(
                                        i.getTipoCombustivel(),
                                        i.getQuantidade(),
                                        i.getValorUnitario(),
                                        i.getValorTotal()
                                )).toList()
                )).toList();
    }
}
