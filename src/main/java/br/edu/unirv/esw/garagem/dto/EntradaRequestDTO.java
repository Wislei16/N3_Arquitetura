package br.edu.unirv.esw.garagem.dto;

import jakarta.validation.constraints.NotBlank;

public record EntradaRequestDTO(
    @NotBlank(message = "A placa do veiculo e obrigatoria")
    String placa
) {
}
