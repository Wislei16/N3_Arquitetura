package br.edu.unirv.esw.garagem.dto;

import br.edu.unirv.esw.garagem.domain.StatusMovimentacao;
import br.edu.unirv.esw.garagem.domain.TipoVeiculo;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MovimentacaoResponseDTO(
    Long id,
    Long veiculoId,
    String placa,
    String modelo,
    TipoVeiculo tipoVeiculo,
    LocalDateTime dataEntrada,
    LocalDateTime dataSaida,
    StatusMovimentacao status,
    BigDecimal valorCobrado
) {
}
