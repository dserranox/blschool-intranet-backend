package ar.com.blschool.repository;

import ar.com.blschool.entity.Inscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {

    @Query("SELECT i FROM Inscripcion i JOIN FETCH i.comision c JOIN FETCH c.curso WHERE i.alumno.aluId = :alumnoId ORDER BY i.insId DESC ")
    List<Inscripcion> findActivasByAlumnoId(@Param("alumnoId") Long alumnoId);
}
