package ar.com.blschool.service;

import ar.com.blschool.entity.Comision;
import ar.com.blschool.entity.Usuario;
import ar.com.blschool.repository.ComisionClaseRepository;
import ar.com.blschool.repository.ComisionRepository;
import ar.com.blschool.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;

/**
 * Quién puede calificar qué: el ADMIN todas las comisiones; una docente sólo aquellas
 * donde figura como titular o suplente en algún horario (COMISION_CLASES).
 */
@Service
public class PermisoCalificacionService {

    private final UsuarioRepository usuarioRepository;
    private final ComisionClaseRepository comisionClaseRepository;
    private final ComisionRepository comisionRepository;

    public PermisoCalificacionService(UsuarioRepository usuarioRepository,
                                      ComisionClaseRepository comisionClaseRepository,
                                      ComisionRepository comisionRepository) {
        this.usuarioRepository = usuarioRepository;
        this.comisionClaseRepository = comisionClaseRepository;
        this.comisionRepository = comisionRepository;
    }

    public boolean esAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    /** 403 si el usuario actual no puede calificar la comisión. */
    public void verificarComision(Long comisionId) {
        if (esAdmin()) {
            return;
        }
        if (!comisionClaseRepository.esDocenteDeComision(comisionId, personaIdActual())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No sos docente de esta comisión");
        }
    }

    /** Comisiones del año que el usuario actual puede calificar, ordenadas por curso y nombre. */
    @Transactional(readOnly = true)
    public List<Comision> comisionesCalificables(Integer anio) {
        List<Comision> comisiones = esAdmin()
                ? comisionRepository.findByComAnioOrderByCursoCurNombreAscComNombreAsc(anio)
                : comisionClaseRepository.findComisionesDeDocente(anio, personaIdActual());
        return comisiones.stream()
                .sorted(Comparator.comparing((Comision c) -> c.getCurso().getCurNombre())
                        .thenComparing(Comision::getComNombre))
                .toList();
    }

    private Long personaIdActual() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByUsernameIgnoreCase(username)
                .map(Usuario::getPersonaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "El usuario no tiene una persona asociada"));
    }
}
