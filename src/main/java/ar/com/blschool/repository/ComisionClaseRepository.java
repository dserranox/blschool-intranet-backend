package ar.com.blschool.repository;

import ar.com.blschool.entity.ComisionClase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ComisionClaseRepository extends JpaRepository<ComisionClase, Long> {

    @Query("SELECT COUNT(DISTINCT cc.comision.comId) FROM ComisionClase cc WHERE cc.docente.perId = :personaId")
    long countComisionesDistintasByDocenteId(@Param("personaId") Long personaId);
}
