package br.edu.unirv.esw.garagem.controller;

import br.edu.unirv.esw.garagem.domain.TipoVeiculo;
import br.edu.unirv.esw.garagem.dto.VeiculoRequestDTO;
import br.edu.unirv.esw.garagem.dto.VeiculoResponseDTO;
import br.edu.unirv.esw.garagem.service.VeiculoComandoService;
import br.edu.unirv.esw.garagem.service.VeiculoConsultaService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/veiculos")
public class VeiculoController {

    private final VeiculoConsultaService consultaService;
    private final VeiculoComandoService comandoService;

    public VeiculoController(
        VeiculoConsultaService consultaService,
        VeiculoComandoService comandoService
    ) {
        this.consultaService = consultaService;
        this.comandoService = comandoService;
    }

    @GetMapping
    public ResponseEntity<Page<VeiculoResponseDTO>> listar(
        @RequestParam(required = false) TipoVeiculo tipo,
        @PageableDefault(size = 20, sort = "id") Pageable pageable
    ) {
        return ResponseEntity.ok(consultaService.listarPaginado(tipo, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VeiculoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(consultaService.buscarPorId(id));
    }

    @GetMapping("/placa/{placa}")
    public ResponseEntity<VeiculoResponseDTO> buscarPorPlaca(@PathVariable String placa) {
        return ResponseEntity.ok(consultaService.buscarPorPlaca(placa));
    }

    @PostMapping
    public ResponseEntity<VeiculoResponseDTO> cadastrar(@Valid @RequestBody VeiculoRequestDTO request) {
        VeiculoResponseDTO criado = comandoService.cadastrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }
}
