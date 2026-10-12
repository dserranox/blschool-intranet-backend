package ar.com.blschool.repository;

import ar.com.blschool.entity.Logro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LogroRepository extends JpaRepository<Logro, Long> {

    List<Logro> findByCursoCurIdOrderByLogOrdenAsc(Long cursoId);
}
