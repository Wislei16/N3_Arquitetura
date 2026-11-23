package br.edu.unirv.esw.garagem;

import static org.junit.jupiter.api.Assertions.assertEquals;

import br.edu.unirv.esw.garagem.service.strategy.TarifaMotoStrategy;
import br.edu.unirv.esw.garagem.service.strategy.TarifaPasseioStrategy;
import java.math.BigDecimal;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class GaragemApplicationTests {

    @Test
    void deveCalcularTarifaConformeEstrategia() {
        TarifaPasseioStrategy passeio = new TarifaPasseioStrategy();
        TarifaMotoStrategy moto = new TarifaMotoStrategy();

        assertEquals(new BigDecimal("20.00"), passeio.calcular(Duration.ofMinutes(95)));
        assertEquals(new BigDecimal("5.00"), moto.calcular(Duration.ofMinutes(30)));
    }
}
