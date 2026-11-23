package br.edu.unirv.esw.garagem.config;

import br.edu.unirv.esw.garagem.domain.Movimentacao;
import br.edu.unirv.esw.garagem.domain.StatusMovimentacao;
import br.edu.unirv.esw.garagem.domain.TipoVeiculo;
import br.edu.unirv.esw.garagem.domain.Veiculo;
import br.edu.unirv.esw.garagem.repository.MovimentacaoRepository;
import br.edu.unirv.esw.garagem.repository.VeiculoRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataInitializer.class);

    private final VeiculoRepository veiculoRepository;
    private final MovimentacaoRepository movimentacaoRepository;
    private final boolean seedEnabled;

    public DataInitializer(
        VeiculoRepository veiculoRepository,
        MovimentacaoRepository movimentacaoRepository,
        @Value("${garagem.seed-enabled:true}") boolean seedEnabled
    ) {
        this.veiculoRepository = veiculoRepository;
        this.movimentacaoRepository = movimentacaoRepository;
        this.seedEnabled = seedEnabled;
    }

    @Override
    public void run(String... args) {
        if (!seedEnabled || veiculoRepository.count() > 0) {
            return;
        }

        String[] modelos = {"Onix", "HB20", "Corolla", "Civic", "CG 160", "Fazer 250", "Hilux", "Strada"};
        String[] marcas = {"Chevrolet", "Hyundai", "Toyota", "Honda", "Honda", "Yamaha", "Toyota", "Fiat"};
        String[] cores = {"Branco", "Prata", "Preto", "Cinza", "Vermelho"};
        TipoVeiculo[] tipos = {
            TipoVeiculo.PASSEIO, TipoVeiculo.PASSEIO, TipoVeiculo.PASSEIO, TipoVeiculo.PASSEIO,
            TipoVeiculo.MOTO, TipoVeiculo.MOTO, TipoVeiculo.UTILITARIO, TipoVeiculo.UTILITARIO
        };

        List<Veiculo> veiculos = new ArrayList<>(1200);
        for (int i = 1; i <= 1200; i++) {
            String placa = String.format("GOA%04d", i);
            int idx = i % modelos.length;
            veiculos.add(new Veiculo(
                placa,
                modelos[idx],
                marcas[idx],
                cores[i % cores.length],
                tipos[idx]
            ));
        }

        List<Veiculo> salvos = veiculoRepository.saveAll(veiculos);

        List<Movimentacao> movimentacoes = new ArrayList<>(1200);
        LocalDateTime base = LocalDateTime.now().minusDays(10);
        for (int i = 0; i < salvos.size(); i++) {
            Movimentacao m = new Movimentacao(salvos.get(i), base.plusMinutes(i * 5L));
            if (i >= 150) {
                m.setDataSaida(m.getDataEntrada().plusHours(2));
                m.setStatus(StatusMovimentacao.FINALIZADO);
                m.setValorCobrado(new BigDecimal("20.00"));
            }
            movimentacoes.add(m);
        }
        movimentacaoRepository.saveAll(movimentacoes);

        LOGGER.info("Carga inicial concluida: {} veiculos e {} movimentacoes.", salvos.size(), movimentacoes.size());
    }
}
