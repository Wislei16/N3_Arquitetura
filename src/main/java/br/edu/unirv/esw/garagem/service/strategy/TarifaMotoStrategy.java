package br.edu.unirv.esw.garagem.service.strategy;

import br.edu.unirv.esw.garagem.domain.TipoVeiculo;
import java.math.BigDecimal;
import java.time.Duration;
import org.springframework.stereotype.Component;

@Component
public class TarifaMotoStrategy implements CalculoTarifaStrategy {

    private static final BigDecimal VALOR_HORA = new BigDecimal("5.00");

    @Override
    public boolean suporta(TipoVeiculo tipo) {
        return TipoVeiculo.MOTO == tipo;
    }

    @Override
    public BigDecimal calcular(Duration permanencia) {
        long horas = Math.max(1, (long) Math.ceil(permanencia.toMinutes() / 60.0));
        return VALOR_HORA.multiply(BigDecimal.valueOf(horas));
    }
}
