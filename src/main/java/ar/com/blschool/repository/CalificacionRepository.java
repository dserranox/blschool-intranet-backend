package ar.com.blschool.repository;

import ar.com.blschool.entity.Calificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CalificacionRepository extends JpaRepository<Calificacion, Long> {

    List<Calificacion> findByEvaluacionEvaId(Long evaluacionId);

    List<Calificacion> findByEvaluacionEvaIdIn(Collection<Long> evaluacionIds);

    Optional<Calificacion> findByEvaluacionEvaIdAndAlumnoAluId(Long evaluacionId, Long alumnoId);

    long countByEvaluacionComisionComAnio(Integer anio);

    @Query("SELECT c FROM Calificacion c JOIN FETCH c.evaluacion e JOIN FETCH e.comision com JOIN FETCH com.curso " +
           "WHERE c.alumno.aluId = :alumnoId " +
           "ORDER BY com.comAnio DESC, com.comId, e.evaFecha ASC NULLS FIRST, e.evaId")
    List<Calificacion> findHistorialByAlumnoId(@Param("alumnoId") Long alumnoId);
}
