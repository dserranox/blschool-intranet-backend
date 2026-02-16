package ar.com.blschool.repository;

import ar.com.blschool.entity.Comision;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComisionRepository extends JpaRepository<Comision, Long> {

    boolean existsByCursoCurId(Long cursoId);

    List<Comision> findByComAnio(Integer anio);

    List<Comision> findByCursoCurId(Long cursoId);
}
