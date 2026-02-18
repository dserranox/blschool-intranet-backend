package ar.com.blschool.repository;

import ar.com.blschool.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    boolean existsByUsernameIgnoreCase(String username);

    Optional<Usuario> findByUsernameIgnoreCase(String username);

    Optional<Usuario> findByPersonaId(Long personaId);

    @Query("SELECT COUNT(u) FROM Usuario u JOIN u.roles r WHERE r.nombre = :rolNombre AND u.activo = true")
    long countActivosByRol(@Param("rolNombre") String rolNombre);
}
