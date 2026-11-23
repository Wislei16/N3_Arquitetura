package br.edu.unirv.esw.garagem.repository;

import br.edu.unirv.esw.garagem.domain.Movimentacao;
import br.edu.unirv.esw.garagem.domain.StatusMovimentacao;
import br.edu.unirv.esw.garagem.dto.MovimentacaoResponseDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MovimentacaoRepository extends JpaRepository<Movimentacao, Long> {

    long countByStatus(StatusMovimentacao status);

    Optional<Movimentacao> findFirstByVeiculoPlacaIgnoreCaseAndStatus(String placa, StatusMovimentacao status);

    boolean existsByVeiculoIdAndStatus(Long veiculoId, StatusMovimentacao status);

    /**
     * Projecao direta em DTO na consulta JPQL para evitar problema N+1
     * e reduzir alocacao de entidades completas no Heap da JVM.
     */
    @Query("""
        SELECT new br.edu.unirv.esw.garagem.dto.MovimentacaoResponseDTO(
            m.id,
            v.id,
            v.placa,
            v.modelo,
            v.tipo,
            m.dataEntrada,
            m.dataSaida,
            m.status,
            m.valorCobrado
        )
        FROM Movimentacao m
        JOIN m.veiculo v
        """)
    Page<MovimentacaoResponseDTO> findAllProjected(Pageable pageable);
}
