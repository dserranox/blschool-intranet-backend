package ar.com.blschool.repository;

import ar.com.blschool.entity.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PersonaRepository extends JpaRepository<Persona, Long> {

    @Query("SELECT DISTINCT p FROM Persona p JOIN Usuario u ON u.personaId = p.perId " +
           "JOIN u.roles r WHERE r.nombre IN :roles")
    List<Persona> findByRoles(@Param("roles") List<String> roles);
}
