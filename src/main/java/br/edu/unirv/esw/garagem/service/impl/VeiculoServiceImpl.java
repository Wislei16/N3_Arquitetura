package br.edu.unirv.esw.garagem.service.impl;

import br.edu.unirv.esw.garagem.domain.TipoVeiculo;
import br.edu.unirv.esw.garagem.domain.Veiculo;
import br.edu.unirv.esw.garagem.dto.VeiculoRequestDTO;
import br.edu.unirv.esw.garagem.dto.VeiculoResponseDTO;
import br.edu.unirv.esw.garagem.exception.NegocioException;
import br.edu.unirv.esw.garagem.exception.RecursoNaoEncontradoException;
import br.edu.unirv.esw.garagem.mapper.VeiculoMapper;
import br.edu.unirv.esw.garagem.repository.VeiculoRepository;
import br.edu.unirv.esw.garagem.service.VeiculoComandoService;
import br.edu.unirv.esw.garagem.service.VeiculoConsultaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Componente stateless gerenciado em escopo Singleton pelo conteiner Spring IoC,
 * evitando reinstanciacoes por requisicao e reduzindo pressao sobre o Heap.
 */
@Service
public class VeiculoServiceImpl implements VeiculoConsultaService, VeiculoComandoService {

    private final VeiculoRepository veiculoRepository;
    private final VeiculoMapper veiculoMapper;

    public VeiculoServiceImpl(VeiculoRepository veiculoRepository, VeiculoMapper veiculoMapper) {
        this.veiculoRepository = veiculoRepository;
        this.veiculoMapper = veiculoMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VeiculoResponseDTO> listarPaginado(TipoVeiculo tipo, Pageable pageable) {
        Page<Veiculo> pagina = (tipo != null)
            ? veiculoRepository.findByTipo(tipo, pageable)
            : veiculoRepository.findAll(pageable);
        return pagina.map(veiculoMapper::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public VeiculoResponseDTO buscarPorId(Long id) {
        Veiculo veiculo = veiculoRepository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Veiculo nao encontrado para o ID: " + id));
        return veiculoMapper.toResponseDTO(veiculo);
    }

    @Override
    @Transactional(readOnly = true)
    public VeiculoResponseDTO buscarPorPlaca(String placa) {
        Veiculo veiculo = veiculoRepository.findByPlacaIgnoreCase(placa.trim())
            .orElseThrow(() -> new RecursoNaoEncontradoException("Veiculo nao encontrado para a placa: " + placa));
        return veiculoMapper.toResponseDTO(veiculo);
    }

    @Override
    @Transactional
    public VeiculoResponseDTO cadastrar(VeiculoRequestDTO request) {
        String placaNormalizada = request.placa().trim().toUpperCase();
        if (veiculoRepository.existsByPlacaIgnoreCase(placaNormalizada)) {
            throw new NegocioException("Ja existe veiculo cadastrado com a placa: " + placaNormalizada);
        }
        Veiculo salvo = veiculoRepository.save(veiculoMapper.toEntity(request));
        return veiculoMapper.toResponseDTO(salvo);
    }
}
