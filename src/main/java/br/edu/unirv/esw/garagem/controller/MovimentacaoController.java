package br.edu.unirv.esw.garagem.controller;

import br.edu.unirv.esw.garagem.dto.EntradaRequestDTO;
import br.edu.unirv.esw.garagem.dto.MovimentacaoResponseDTO;
import br.edu.unirv.esw.garagem.service.MovimentacaoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/movimentacoes")
public class MovimentacaoController {

    private final MovimentacaoService movimentacaoService;

    public MovimentacaoController(MovimentacaoService movimentacaoService) {
        this.movimentacaoService = movimentacaoService;
    }

    @GetMapping
    public ResponseEntity<Page<MovimentacaoResponseDTO>> listarHistorico(
        @PageableDefault(size = 20, sort = "id") Pageable pageable
    ) {
        return ResponseEntity.ok(movimentacaoService.listarHistorico(pageable));
    }

    @PostMapping("/entrada")
    public ResponseEntity<MovimentacaoResponseDTO> registrarEntrada(@Valid @RequestBody EntradaRequestDTO request) {
        MovimentacaoResponseDTO criada = movimentacaoService.registrarEntrada(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }

    @PostMapping("/saida/{placa}")
    public ResponseEntity<MovimentacaoResponseDTO> registrarSaida(@PathVariable String placa) {
        return ResponseEntity.ok(movimentacaoService.registrarSaida(placa));
    }
}
