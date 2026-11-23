package br.edu.unirv.esw.garagem.dto;

import br.edu.unirv.esw.garagem.domain.TipoVeiculo;
import java.time.LocalDateTime;

public record VeiculoResponseDTO(
    Long id,
    String placa,
    String modelo,
    String marca,
    String cor,
    TipoVeiculo tipo,
    LocalDateTime dataCadastro
) {
}
