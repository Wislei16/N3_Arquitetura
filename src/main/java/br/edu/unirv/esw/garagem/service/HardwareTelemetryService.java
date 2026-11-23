package br.edu.unirv.esw.garagem.service;

import br.edu.unirv.esw.garagem.dto.RecursosHardwareDTO;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class HardwareTelemetryService {

    private static final long BYTES_POR_MB = 1024L * 1024L;

    private final int hikariPoolMaxSize;
    private final int tomcatMaxThreads;

    public HardwareTelemetryService(
        @Value("${spring.datasource.hikari.maximum-pool-size:8}") int hikariPoolMaxSize,
        @Value("${server.tomcat.threads.max:50}") int tomcatMaxThreads
    ) {
        this.hikariPoolMaxSize = hikariPoolMaxSize;
        this.tomcatMaxThreads = tomcatMaxThreads;
    }

    public RecursosHardwareDTO coletarMetricas() {
        Runtime runtime = Runtime.getRuntime();
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryMXBean.getHeapMemoryUsage();
        MemoryUsage nonHeapUsage = memoryMXBean.getNonHeapMemoryUsage();

        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
        long totalColetas = 0L;
        long tempoTotalMs = 0L;
        for (GarbageCollectorMXBean gc : gcBeans) {
            if (gc.getCollectionCount() > 0) {
                totalColetas += gc.getCollectionCount();
            }
            if (gc.getCollectionTime() > 0) {
                tempoTotalMs += gc.getCollectionTime();
            }
        }

        List<String> coletores = gcBeans.stream()
            .map(GarbageCollectorMXBean::getName)
            .toList();

        return new RecursosHardwareDTO(
            runtime.availableProcessors(),
            runtime.maxMemory() / BYTES_POR_MB,
            heapUsage.getUsed() / BYTES_POR_MB,
            heapUsage.getMax() / BYTES_POR_MB,
            nonHeapUsage.getUsed() / BYTES_POR_MB,
            totalColetas,
            tempoTotalMs,
            coletores,
            hikariPoolMaxSize,
            tomcatMaxThreads
        );
    }
}
