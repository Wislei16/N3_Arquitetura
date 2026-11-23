package br.edu.unirv.esw.garagem.service;

import br.edu.unirv.esw.garagem.dto.VeiculoRequestDTO;
import br.edu.unirv.esw.garagem.dto.VeiculoResponseDTO;

public interface VeiculoComandoService {

    VeiculoResponseDTO cadastrar(VeiculoRequestDTO request);
}
