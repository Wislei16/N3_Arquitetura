package br.edu.unirv.esw.garagem.repository;

import br.edu.unirv.esw.garagem.domain.TipoVeiculo;
import br.edu.unirv.esw.garagem.domain.Veiculo;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VeiculoRepository extends JpaRepository<Veiculo, Long> {

    Optional<Veiculo> findByPlacaIgnoreCase(String placa);

    boolean existsByPlacaIgnoreCase(String placa);

    Page<Veiculo> findByTipo(TipoVeiculo tipo, Pageable pageable);
}
