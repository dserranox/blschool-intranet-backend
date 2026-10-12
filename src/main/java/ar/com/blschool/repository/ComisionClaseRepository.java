package ar.com.blschool.repository;

import ar.com.blschool.entity.Comision;
import ar.com.blschool.entity.ComisionClase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ComisionClaseRepository extends JpaRepository<ComisionClase, Long> {

    @Query("SELECT COUNT(DISTINCT cc.comision.comId) FROM ComisionClase cc WHERE cc.docente.perId = :personaId")
    long countComisionesDistintasByDocenteId(@Param("personaId") Long personaId);

    @Query("SELECT COUNT(cc) > 0 FROM ComisionClase cc LEFT JOIN cc.docente d LEFT JOIN cc.docenteSuplente s " +
           "WHERE cc.comision.comId = :comisionId AND (d.perId = :personaId OR s.perId = :personaId)")
    boolean esDocenteDeComision(@Param("comisionId") Long comisionId, @Param("personaId") Long personaId);

    @Query("SELECT DISTINCT c FROM ComisionClase cc JOIN cc.comision c LEFT JOIN cc.docente d LEFT JOIN cc.docenteSuplente s " +
           "WHERE c.comAnio = :anio AND (d.perId = :personaId OR s.perId = :personaId)")
    List<Comision> findComisionesDeDocente(@Param("anio") Integer anio, @Param("personaId") Long personaId);
}
