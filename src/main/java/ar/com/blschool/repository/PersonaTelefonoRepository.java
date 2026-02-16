package ar.com.blschool.repository;

import ar.com.blschool.entity.PersonaTelefono;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PersonaTelefonoRepository extends JpaRepository<PersonaTelefono, Long> {

}
