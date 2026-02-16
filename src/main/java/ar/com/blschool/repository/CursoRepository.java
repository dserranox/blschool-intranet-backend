package ar.com.blschool.repository;

import ar.com.blschool.entity.Curso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CursoRepository extends JpaRepository<Curso, Long> {

    @Query("SELECT c FROM Curso c WHERE LOWER(c.curCodigo) LIKE LOWER(CONCAT('%', :filtro, '%')) " +
           "OR LOWER(c.curNombre) LIKE LOWER(CONCAT('%', :filtro, '%'))")
    List<Curso> findByFiltro(@Param("filtro") String filtro);
}
