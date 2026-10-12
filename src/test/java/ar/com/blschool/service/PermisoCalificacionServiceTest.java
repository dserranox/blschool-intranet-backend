package ar.com.blschool.service;

import ar.com.blschool.entity.Usuario;
import ar.com.blschool.repository.ComisionClaseRepository;
import ar.com.blschool.repository.ComisionRepository;
import ar.com.blschool.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PermisoCalificacionServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private ComisionClaseRepository comisionClaseRepository;
    @Mock
    private ComisionRepository comisionRepository;
    @InjectMocks
    private PermisoCalificacionService permisos;

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    private void loguear(String usuario, String... roles) {
        var authorities = Arrays.stream(roles).map(r -> new SimpleGrantedAuthority("ROLE_" + r)).toList();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(usuario, null, authorities));
    }

    private void usuarioConPersona(String username, Long personaId) {
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPersonaId(personaId);
        when(usuarioRepository.findByUsernameIgnoreCase(username)).thenReturn(Optional.of(usuario));
    }

    @Test
    void adminPuedeCalificarCualquierComision() {
        loguear("admin", "ADMIN");
        assertThatCode(() -> permisos.verificarComision(99L)).doesNotThrowAnyException();
        verifyNoInteractions(usuarioRepository, comisionClaseRepository);
    }

    @Test
    void docentePuedeCalificarSusComisiones() {
        loguear("rbrizzio", "DOCENTE");
        usuarioConPersona("rbrizzio", 30L);
        when(comisionClaseRepository.esDocenteDeComision(5L, 30L)).thenReturn(true);
        assertThatCode(() -> permisos.verificarComision(5L)).doesNotThrowAnyException();
    }

    @Test
    void docenteNoPuedeCalificarComisionAjena() {
        loguear("rbrizzio", "DOCENTE");
        usuarioConPersona("rbrizzio", 30L);
        when(comisionClaseRepository.esDocenteDeComision(6L, 30L)).thenReturn(false);
        assertThatThrownBy(() -> permisos.verificarComision(6L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("403");
    }

    @Test
    void usuarioSinPersonaNoPuedeCalificar() {
        loguear("huerfano", "DOCENTE");
        usuarioConPersona("huerfano", null);
        assertThatThrownBy(() -> permisos.verificarComision(5L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("403");
    }
}
