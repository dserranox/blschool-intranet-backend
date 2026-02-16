package ar.com.blschool.repository;

import ar.com.blschool.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    boolean existsByUsernameIgnoreCase(String username);

    Optional<Usuario> findByUsernameIgnoreCase(String username);

    Optional<Usuario> findByPersonaId(Long personaId);
}
