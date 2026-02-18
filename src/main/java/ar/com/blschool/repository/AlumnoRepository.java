package ar.com.blschool.repository;

import ar.com.blschool.entity.Alumno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AlumnoRepository extends JpaRepository<Alumno, Long> {

    long countByAluEstado(String estado);

    @Query("SELECT DISTINCT a FROM Alumno a JOIN FETCH a.persona p LEFT JOIN FETCH p.personasTelefonos WHERE a.aluEstado = :estado ORDER BY p.perApellido, p.perNombres")
    List<Alumno> findByEstadoWithPersona(@Param("estado") String estado);

    @Query("SELECT DISTINCT a FROM Alumno a JOIN FETCH a.persona p LEFT JOIN FETCH p.personasTelefonos ORDER BY p.perApellido, p.perNombres")
    List<Alumno> findAllWithPersona();
}
