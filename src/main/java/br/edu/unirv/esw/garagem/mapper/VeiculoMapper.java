package br.edu.unirv.esw.garagem.mapper;

import br.edu.unirv.esw.garagem.domain.Veiculo;
import br.edu.unirv.esw.garagem.dto.VeiculoRequestDTO;
import br.edu.unirv.esw.garagem.dto.VeiculoResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class VeiculoMapper {

    public Veiculo toEntity(VeiculoRequestDTO dto) {
        return new Veiculo(
            dto.placa().trim().toUpperCase(),
            dto.modelo().trim(),
            dto.marca().trim(),
            dto.cor().trim(),
            dto.tipo()
        );
    }

    public VeiculoResponseDTO toResponseDTO(Veiculo entity) {
        return new VeiculoResponseDTO(
            entity.getId(),
            entity.getPlaca(),
            entity.getModelo(),
            entity.getMarca(),
            entity.getCor(),
            entity.getTipo(),
            entity.getDataCadastro()
        );
    }
}
