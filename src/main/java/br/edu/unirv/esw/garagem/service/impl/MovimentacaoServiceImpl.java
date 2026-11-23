package br.edu.unirv.esw.garagem.service.impl;

import br.edu.unirv.esw.garagem.domain.Movimentacao;
import br.edu.unirv.esw.garagem.domain.StatusMovimentacao;
import br.edu.unirv.esw.garagem.domain.TipoVeiculo;
import br.edu.unirv.esw.garagem.domain.Veiculo;
import br.edu.unirv.esw.garagem.dto.EntradaRequestDTO;
import br.edu.unirv.esw.garagem.dto.MovimentacaoResponseDTO;
import br.edu.unirv.esw.garagem.exception.NegocioException;
import br.edu.unirv.esw.garagem.exception.RecursoNaoEncontradoException;
import br.edu.unirv.esw.garagem.mapper.MovimentacaoMapper;
import br.edu.unirv.esw.garagem.repository.MovimentacaoRepository;
import br.edu.unirv.esw.garagem.repository.VeiculoRepository;
import br.edu.unirv.esw.garagem.service.MovimentacaoService;
import br.edu.unirv.esw.garagem.service.strategy.CalculoTarifaStrategy;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MovimentacaoServiceImpl implements MovimentacaoService {

    private final MovimentacaoRepository movimentacaoRepository;
    private final VeiculoRepository veiculoRepository;
    private final MovimentacaoMapper movimentacaoMapper;
    private final List<CalculoTarifaStrategy> estrategiasTarifa;
    private final int capacidadeTotal;

    public MovimentacaoServiceImpl(
        MovimentacaoRepository movimentacaoRepository,
        VeiculoRepository veiculoRepository,
        MovimentacaoMapper movimentacaoMapper,
        List<CalculoTarifaStrategy> estrategiasTarifa,
        @Value("${garagem.capacidade-total:500}") int capacidadeTotal
    ) {
        this.movimentacaoRepository = movimentacaoRepository;
        this.veiculoRepository = veiculoRepository;
        this.movimentacaoMapper = movimentacaoMapper;
        this.estrategiasTarifa = estrategiasTarifa;
        this.capacidadeTotal = capacidadeTotal;
    }

    @Override
    @Transactional
    public MovimentacaoResponseDTO registrarEntrada(EntradaRequestDTO request) {
        String placa = request.placa().trim().toUpperCase();
        Veiculo veiculo = veiculoRepository.findByPlacaIgnoreCase(placa)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Veiculo nao cadastrado: " + placa));

        if (movimentacaoRepository.existsByVeiculoIdAndStatus(veiculo.getId(), StatusMovimentacao.ESTACIONADO)) {
            throw new NegocioException("O veiculo " + placa + " ja possui entrada ativa na garagem.");
        }

        long ocupadas = movimentacaoRepository.countByStatus(StatusMovimentacao.ESTACIONADO);
        if (ocupadas >= capacidadeTotal) {
            throw new NegocioException("Capacidade maxima da garagem atingida (" + capacidadeTotal + " vagas).");
        }

        Movimentacao mov = new Movimentacao(veiculo, LocalDateTime.now());
        Movimentacao salva = movimentacaoRepository.save(mov);
        return movimentacaoMapper.toResponseDTO(salva, veiculo);
    }

    @Override
    @Transactional
    public MovimentacaoResponseDTO registrarSaida(String placa) {
        String placaNormalizada = placa.trim().toUpperCase();
        Movimentacao mov = movimentacaoRepository
            .findFirstByVeiculoPlacaIgnoreCaseAndStatus(placaNormalizada, StatusMovimentacao.ESTACIONADO)
            .orElseThrow(() -> new RecursoNaoEncontradoException(
                "Nenhuma movimentacao ativa encontrada para a placa: " + placaNormalizada
            ));

        LocalDateTime agora = LocalDateTime.now();
        Duration permanencia = Duration.between(mov.getDataEntrada(), agora);
        Veiculo veiculo = mov.getVeiculo();

        BigDecimal valor = selecionarEstrategia(veiculo.getTipo()).calcular(permanencia);

        mov.setDataSaida(agora);
        mov.setStatus(StatusMovimentacao.FINALIZADO);
        mov.setValorCobrado(valor);

        Movimentacao atualizada = movimentacaoRepository.save(mov);
        return movimentacaoMapper.toResponseDTO(atualizada, veiculo);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovimentacaoResponseDTO> listarHistorico(Pageable pageable) {
        return movimentacaoRepository.findAllProjected(pageable);
    }

    private CalculoTarifaStrategy selecionarEstrategia(TipoVeiculo tipo) {
        return estrategiasTarifa.stream()
            .filter(estrategia -> estrategia.suporta(tipo))
            .findFirst()
            .orElseThrow(() -> new NegocioException("Nenhuma estrategia de tarifa configurada para: " + tipo));
    }
}
