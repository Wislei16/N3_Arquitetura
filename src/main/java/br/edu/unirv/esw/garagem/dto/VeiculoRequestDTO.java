package br.edu.unirv.esw.garagem.dto;

import br.edu.unirv.esw.garagem.domain.TipoVeiculo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record VeiculoRequestDTO(
    @NotBlank(message = "A placa e obrigatoria")
    @Size(min = 7, max = 10, message = "A placa deve ter entre 7 e 10 caracteres")
    String placa,

    @NotBlank(message = "O modelo e obrigatorio")
    String modelo,

    @NotBlank(message = "A marca e obrigatoria")
    String marca,

    @NotBlank(message = "A cor e obrigatoria")
    String cor,

    @NotNull(message = "O tipo do veiculo e obrigatorio")
    TipoVeiculo tipo
) {
}
