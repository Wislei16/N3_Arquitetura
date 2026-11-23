package br.edu.unirv.esw.garagem.controller;

import br.edu.unirv.esw.garagem.dto.RecursosHardwareDTO;
import br.edu.unirv.esw.garagem.service.HardwareTelemetryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sistema")
public class HardwareTelemetryController {

    private final HardwareTelemetryService telemetryService;

    public HardwareTelemetryController(HardwareTelemetryService telemetryService) {
        this.telemetryService = telemetryService;
    }

    @GetMapping("/recursos")
    public ResponseEntity<RecursosHardwareDTO> obterRecursos() {
        return ResponseEntity.ok(telemetryService.coletarMetricas());
    }
}
