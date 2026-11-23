package br.edu.unirv.esw.garagem.service;

import br.edu.unirv.esw.garagem.dto.EntradaRequestDTO;
import br.edu.unirv.esw.garagem.dto.MovimentacaoResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MovimentacaoService {

    MovimentacaoResponseDTO registrarEntrada(EntradaRequestDTO request);

    MovimentacaoResponseDTO registrarSaida(String placa);

    Page<MovimentacaoResponseDTO> listarHistorico(Pageable pageable);
}
