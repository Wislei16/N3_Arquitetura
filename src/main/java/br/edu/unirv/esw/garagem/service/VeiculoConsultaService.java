package br.edu.unirv.esw.garagem.service;

import br.edu.unirv.esw.garagem.domain.TipoVeiculo;
import br.edu.unirv.esw.garagem.dto.VeiculoResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VeiculoConsultaService {

    Page<VeiculoResponseDTO> listarPaginado(TipoVeiculo tipo, Pageable pageable);

    VeiculoResponseDTO buscarPorId(Long id);

    VeiculoResponseDTO buscarPorPlaca(String placa);
}
