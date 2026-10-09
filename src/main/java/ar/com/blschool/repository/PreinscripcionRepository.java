package ar.com.blschool.repository;

import ar.com.blschool.entity.Preinscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PreinscripcionRepository extends JpaRepository<Preinscripcion, Long> {

    Optional<Preinscripcion> findByAlumnoAluIdAndPreAnio(Long alumnoId, Integer anio);

    List<Preinscripcion> findByInscripcionInsId(Long inscripcionId);

    @Query("SELECT p FROM Preinscripcion p JOIN FETCH p.alumno a JOIN FETCH a.persona per " +
           "LEFT JOIN FETCH p.comision c LEFT JOIN FETCH c.curso " +
           "WHERE p.preAnio = :anio ORDER BY per.perApellido, per.perNombres")
    List<Preinscripcion> findByAnioWithAlumno(@Param("anio") Integer anio);

    @Query("SELECT p FROM Preinscripcion p LEFT JOIN FETCH p.comision c LEFT JOIN FETCH c.curso " +
           "WHERE p.alumno.aluId = :alumnoId ORDER BY p.preAnio DESC")
    List<Preinscripcion> findByAlumnoId(@Param("alumnoId") Long alumnoId);

    @Query("SELECT DISTINCT p.preAnio FROM Preinscripcion p ORDER BY p.preAnio")
    List<Integer> findDistinctAnios();
}
