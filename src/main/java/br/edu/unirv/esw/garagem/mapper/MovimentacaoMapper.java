package br.edu.unirv.esw.garagem.mapper;

import br.edu.unirv.esw.garagem.domain.Movimentacao;
import br.edu.unirv.esw.garagem.domain.Veiculo;
import br.edu.unirv.esw.garagem.dto.MovimentacaoResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class MovimentacaoMapper {

    public MovimentacaoResponseDTO toResponseDTO(Movimentacao mov, Veiculo veiculo) {
        return new MovimentacaoResponseDTO(
            mov.getId(),
            veiculo.getId(),
            veiculo.getPlaca(),
            veiculo.getModelo(),
            veiculo.getTipo(),
            mov.getDataEntrada(),
            mov.getDataSaida(),
            mov.getStatus(),
            mov.getValorCobrado()
        );
    }
}
