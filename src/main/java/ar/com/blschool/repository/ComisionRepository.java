package ar.com.blschool.repository;

import ar.com.blschool.entity.Comision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ComisionRepository extends JpaRepository<Comision, Long> {

    boolean existsByCursoCurId(Long cursoId);

    List<Comision> findByComAnioOrderByCursoCurNombreAscComNombreAsc(Integer anio);

    List<Comision> findByCursoCurId(Long cursoId);

    List<Comision> findByCursoCurIdAndComActivaTrue(Long cursoId);

    long countByComActivaTrue();

    @Query("SELECT c FROM Comision c JOIN FETCH c.curso WHERE c.comActiva = true ORDER BY c.curso.curNombre, c.comNombre")
    List<Comision> findAllActivas();

    @Query("SELECT DISTINCT c.comAnio FROM Comision c ORDER BY c.comAnio")
    List<Integer> findDistinctAnios();
}
