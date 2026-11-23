package br.edu.unirv.esw.garagem.dto;

import java.util.List;

public record RecursosHardwareDTO(
    int nucleosDisponiveisCgroups,
    long memoriaMaxJvmMB,
    long heapUsadoMB,
    long heapMaximoMB,
    long nonHeapUsadoMB,
    long totalColetasGC,
    long tempoTotalGcMs,
    List<String> coletoresAtivos,
    int hikariPoolMaxSize,
    int tomcatMaxThreads
) {
}
