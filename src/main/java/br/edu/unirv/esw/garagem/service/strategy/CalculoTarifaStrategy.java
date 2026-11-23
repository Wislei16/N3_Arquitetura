package br.edu.unirv.esw.garagem.service.strategy;

import br.edu.unirv.esw.garagem.domain.TipoVeiculo;
import java.math.BigDecimal;
import java.time.Duration;

public interface CalculoTarifaStrategy {

    boolean suporta(TipoVeiculo tipo);

    BigDecimal calcular(Duration permanencia);
}
