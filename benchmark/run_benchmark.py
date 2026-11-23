#!/usr/bin/env python3
'''
Script automatizado de medicao de desempenho sob diferentes limites de CPU e memoria (Docker cgroups).
Disciplina: Arquitetura de Software (ESW430) - UniRV - Tema 10
Autor: Wislei Dantas da Silva Costa

Executa os tres perfis definidos na fundamentacao teorica:
- Perfil A: 0.25 vCPU | 256 MB RAM | Pool HikariCP = 4 | Tomcat Max Threads = 20
- Perfil B: 0.50 vCPU | 512 MB RAM | Pool HikariCP = 8 | Tomcat Max Threads = 50
- Perfil C: 2.00 vCPUs | 1024 MB RAM | Pool HikariCP = 15 | Tomcat Max Threads = 200
'''

import concurrent.futures
import json
import os
import statistics
import subprocess
import time
import urllib.request

IMAGE_NAME = "garagem-veiculos-n3:latest"
CONTAINER_NAME = "garagem-benchmark-runner"
BASE_URL = "http://localhost:8080"

PERFIS = [
    {
        "id": "Perfil A",
        "descricao": "Ambiente Severamente Restrito (0.25 vCPU / 256 MB RAM)",
        "cpus": "0.25",
        "memory": "256m",
        "db_pool_size": "4",
        "tomcat_threads": "20",
        "java_opts": "-XX:+UseG1GC -XX:MaxRAMPercentage=75.0",
    },
    {
        "id": "Perfil B",
        "descricao": "Ambiente Intermediario (0.50 vCPU / 512 MB RAM)",
        "cpus": "0.50",
        "memory": "512m",
        "db_pool_size": "8",
        "tomcat_threads": "50",
        "java_opts": "-XX:+UseG1GC -XX:MaxRAMPercentage=75.0",
    },
    {
        "id": "Perfil C",
        "descricao": "Ambiente de Referencia Adequado (2.00 vCPUs / 1024 MB RAM)",
        "cpus": "2.00",
        "memory": "1024m",
        "db_pool_size": "15",
        "tomcat_threads": "200",
        "java_opts": "-XX:+UseG1GC -XX:MaxRAMPercentage=75.0",
    },
]

TOTAL_REQUISICOES = 400
CONCORRENCIA = 16


def run_cmd(cmd, check=True):
    return subprocess.run(cmd, capture_output=True, text=True, check=check)


def remover_conteiner_existente():
    subprocess.run(["docker", "rm", "-f", CONTAINER_NAME], capture_output=True, text=True)


def aguardar_inicializacao(timeout_segundos=120):
    inicio = time.perf_counter()
    url = f"{BASE_URL}/api/sistema/recursos"
    while (time.perf_counter() - inicio) < timeout_segundos:
        inspect = run_cmd(
            ["docker", "inspect", "-f", "{{.State.Running}}|{{.State.OOMKilled}}", CONTAINER_NAME],
            check=False,
        )
        status = inspect.stdout.strip()
        if status.startswith("false"):
            oom = status.endswith("true")
            raise RuntimeError(
                f"Conteiner encerrou durante a inicializacao (OOMKilled={oom})."
            )
        try:
            with urllib.request.urlopen(url, timeout=2) as resp:
                if resp.status == 200:
                    return time.perf_counter() - inicio
        except Exception:
            time.sleep(0.25)
    raise TimeoutError(f"Aplicacao nao respondeu em {timeout_segundos} segundos.")


def obter_telemetria_jvm():
    url = f"{BASE_URL}/api/sistema/recursos"
    with urllib.request.urlopen(url, timeout=5) as resp:
        return json.loads(resp.read().decode("utf-8"))


def obter_docker_stats():
    res = run_cmd(
        [
            "docker",
            "stats",
            "--no-stream",
            "--format",
            "{{.CPUPerc}}|{{.MemUsage}}|{{.MemPerc}}",
            CONTAINER_NAME,
        ],
        check=False,
    )
    partes = res.stdout.strip().split("|")
    if len(partes) == 3:
        return {"cpu_perc": partes[0], "mem_usage": partes[1], "mem_perc": partes[2]}
    return {"cpu_perc": "N/A", "mem_usage": "N/A", "mem_perc": "N/A"}


def obter_throttling_cgroups():
    cmd = (
        "if [ -f /sys/fs/cgroup/cpu.stat ]; then cat /sys/fs/cgroup/cpu.stat; "
        "elif [ -f /sys/fs/cgroup/cpu/cpu.stat ]; then cat /sys/fs/cgroup/cpu/cpu.stat; fi"
    )
    res = run_cmd(["docker", "exec", CONTAINER_NAME, "sh", "-c", cmd], check=False)
    stats = {}
    for linha in res.stdout.splitlines():
        partes = linha.strip().split()
        if len(partes) == 2:
            stats[partes[0]] = int(partes[1])
    return stats


def executar_requisicao_mista(indice):
    '''
    Alterna entre consultas paginadas (I/O + serializacao JSON) e consultas pontuais
    para exercitar CPU, Heap e pool HikariCP de forma realista.
    '''
    if indice % 4 == 0:
        url = f"{BASE_URL}/api/movimentacoes?page=0&size=30"
    elif indice % 4 == 1:
        url = f"{BASE_URL}/api/veiculos?page={indice % 10}&size=40"
    elif indice % 4 == 2:
        url = f"{BASE_URL}/api/veiculos?tipo=PASSEIO&page=0&size=30"
    else:
        veiculo_id = (indice % 500) + 1
        url = f"{BASE_URL}/api/veiculos/{veiculo_id}"

    inicio = time.perf_counter()
    try:
        with urllib.request.urlopen(url, timeout=15) as resp:
            _ = resp.read()
            latencia_ms = (time.perf_counter() - inicio) * 1000.0
            return (True, latencia_ms, resp.status)
    except Exception as exc:
        latencia_ms = (time.perf_counter() - inicio) * 1000.0
        return (False, latencia_ms, str(exc))


def percentil(dados_ordenados, p):
    if not dados_ordenados:
        return 0.0
    k = (len(dados_ordenados) - 1) * (p / 100.0)
    f = int(k)
    c = min(f + 1, len(dados_ordenados) - 1)
    if f == c:
        return dados_ordenados[f]
    return dados_ordenados[f] + (dados_ordenados[c] - dados_ordenados[f]) * (k - f)


def executar_perfil(perfil):
    print("\n=================================================================")
    print(f"Iniciando {perfil['id']}: {perfil['descricao']}")
    print("=================================================================")

    remover_conteiner_existente()

    cmd = [
        "docker",
        "run",
        "-d",
        "--name",
        CONTAINER_NAME,
        "-p",
        "8080:8080",
        f"--cpus={perfil['cpus']}",
        f"--memory={perfil['memory']}",
        f"--memory-swap={perfil['memory']}",
        "-e",
        f"DB_POOL_SIZE={perfil['db_pool_size']}",
        "-e",
        f"TOMCAT_MAX_THREADS={perfil['tomcat_threads']}",
        "-e",
        f"JAVA_TOOL_OPTIONS={perfil['java_opts']}",
        IMAGE_NAME,
    ]
    run_cmd(cmd)

    try:
        tempo_startup = aguardar_inicializacao()
        print(f"[OK] Aplicacao inicializada em {tempo_startup:.2f} s")

        print("[..] Executando aquecimento da JVM (40 requisicoes)...")
        with concurrent.futures.ThreadPoolExecutor(max_workers=8) as pool:
            list(pool.map(executar_requisicao_mista, range(40)))

        telemetria_antes = obter_telemetria_jvm()
        cgroup_antes = obter_throttling_cgroups()

        print(f"[..] Executando carga concorrente ({TOTAL_REQUISICOES} reqs, {CONCORRENCIA} threads)...")
        inicio_carga = time.perf_counter()
        with concurrent.futures.ThreadPoolExecutor(max_workers=CONCORRENCIA) as pool:
            resultados = list(pool.map(executar_requisicao_mista, range(TOTAL_REQUISICOES)))
        duracao_total_s = time.perf_counter() - inicio_carga

        telemetria_depois = obter_telemetria_jvm()
        cgroup_depois = obter_throttling_cgroups()
        docker_stats = obter_docker_stats()

        latencias_ok = sorted([r[1] for r in resultados if r[0]])
        falhas = len(resultados) - len(latencias_ok)
        vazao_rps = len(latencias_ok) / duracao_total_s if duracao_total_s > 0 else 0.0

        delta_gc_count = telemetria_depois["totalColetasGC"] - telemetria_antes["totalColetasGC"]
        delta_gc_time_ms = telemetria_depois["tempoTotalGcMs"] - telemetria_antes["tempoTotalGcMs"]

        throttled_usec = 0
        if "throttled_usec" in cgroup_depois and "throttled_usec" in cgroup_antes:
            throttled_usec = cgroup_depois["throttled_usec"] - cgroup_antes["throttled_usec"]
        elif "throttled_time" in cgroup_depois and "throttled_time" in cgroup_antes:
            throttled_usec = (cgroup_depois["throttled_time"] - cgroup_antes["throttled_time"]) // 1000

        resumo = {
            "perfil": perfil["id"],
            "cpus": perfil["cpus"],
            "memory": perfil["memory"],
            "pool_hikari": perfil["db_pool_size"],
            "nucleos_vistos_jvm": telemetria_depois["nucleosDisponiveisCgroups"],
            "startup_s": round(tempo_startup, 2),
            "latencia_media_ms": round(statistics.mean(latencias_ok), 2) if latencias_ok else 0.0,
            "latencia_p50_ms": round(percentil(latencias_ok, 50), 2),
            "latencia_p95_ms": round(percentil(latencias_ok, 95), 2),
            "latencia_p99_ms": round(percentil(latencias_ok, 99), 2),
            "vazao_rps": round(vazao_rps, 2),
            "falhas": falhas,
            "heap_usado_mb": telemetria_depois["heapUsadoMB"],
            "heap_max_mb": telemetria_depois["heapMaximoMB"],
            "non_heap_mb": telemetria_depois["nonHeapUsadoMB"],
            "delta_gc_coletas": delta_gc_count,
            "delta_gc_tempo_ms": delta_gc_time_ms,
            "cpu_throttled_ms": round(throttled_usec / 1000.0, 2),
            "docker_mem_usage": docker_stats["mem_usage"],
            "status_execucao": "CONCLUIDO",
        }
        print(
            f"[RESULTADO] Startup={resumo['startup_s']}s | Media={resumo['latencia_media_ms']}ms | "
            f"p95={resumo['latencia_p95_ms']}ms | p99={resumo['latencia_p99_ms']}ms | "
            f"Vazao={resumo['vazao_rps']} req/s | GC={delta_gc_count}x ({delta_gc_time_ms}ms) | "
            f"Throttling={resumo['cpu_throttled_ms']}ms"
        )
        return resumo

    except Exception as exc:
        print(f"[ERRO] Falha no {perfil['id']}: {exc}")
        return {
            "perfil": perfil["id"],
            "cpus": perfil["cpus"],
            "memory": perfil["memory"],
            "pool_hikari": perfil["db_pool_size"],
            "status_execucao": f"FALHA ({exc})",
        }
    finally:
        remover_conteiner_existente()


def gerar_relatorio_markdown(resultados):
    linhas = [
        "# Resultados Experimentais - Medicao de Hardware (Docker cgroups)\n",
        "| Perfil | CPU / RAM | Pool Hikari | Startup (s) | Latencia Media (ms) | Latencia p95 (ms) | Latencia p99 (ms) | Vazao (req/s) | Coletas GC (Carga) | Tempo GC (ms) | CPU Throttling (ms) | Memoria RSS Docker |",
        "| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |",
    ]
    for r in resultados:
        if r.get("status_execucao") != "CONCLUIDO":
            linhas.append(
                f"| {r['perfil']} | {r['cpus']} vCPU / {r['memory']} | {r['pool_hikari']} | "
                f"ERRO | - | - | - | - | - | - | - | {r['status_execucao']} |"
            )
        else:
            linhas.append(
                f"| {r['perfil']} | {r['cpus']} vCPU / {r['memory']} | {r['pool_hikari']} | "
                f"{r['startup_s']} | {r['latencia_media_ms']} | {r['latencia_p95_ms']} | "
                f"{r['latencia_p99_ms']} | {r['vazao_rps']} | {r['delta_gc_coletas']} | "
                f"{r['delta_gc_tempo_ms']} | {r['cpu_throttled_ms']} | {r['docker_mem_usage']} |"
            )

    out_path = os.path.join(os.path.dirname(__file__), "resultados_benchmark.md")
    with open(out_path, "w", encoding="utf-8") as f:
        f.write("\n".join(linhas) + "\n")
    print(f"\nTabela Markdown salva em: {out_path}")


def main():
    check_img = run_cmd(["docker", "image", "inspect", IMAGE_NAME], check=False)
    if check_img.returncode != 0:
        print(f"Imagem '{IMAGE_NAME}' nao encontrada. Construindo imagem Docker...")
        raiz = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
        run_cmd(["docker", "build", "-t", IMAGE_NAME, raiz])

    resultados = []
    for perfil in PERFIS:
        resultados.append(executar_perfil(perfil))

    json_path = os.path.join(os.path.dirname(__file__), "resultados_benchmark.json")
    with open(json_path, "w", encoding="utf-8") as f:
        json.dump(resultados, f, indent=2, ensure_ascii=False)

    gerar_relatorio_markdown(resultados)


if __name__ == "__main__":
    main()
