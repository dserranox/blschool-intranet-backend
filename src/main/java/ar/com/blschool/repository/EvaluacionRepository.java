package ar.com.blschool.repository;

import ar.com.blschool.entity.Evaluacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EvaluacionRepository extends JpaRepository<Evaluacion, Long> {

    Optional<Evaluacion> findByComisionComIdAndEvaTipo(Long comisionId, String tipo);

    List<Evaluacion> findByComisionComIdAndEvaTipoOrderByEvaFechaAscEvaIdAsc(Long comisionId, String tipo);
}
