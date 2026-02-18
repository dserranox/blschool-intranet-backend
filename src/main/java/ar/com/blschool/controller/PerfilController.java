package ar.com.blschool.controller;

import ar.com.blschool.dto.DashboardDTO;
import ar.com.blschool.dto.PerfilDTO;
import ar.com.blschool.entity.Persona;
import ar.com.blschool.entity.Rol;
import ar.com.blschool.entity.Usuario;
import ar.com.blschool.repository.AlumnoRepository;
import ar.com.blschool.repository.ComisionRepository;
import ar.com.blschool.repository.CursoRepository;
import ar.com.blschool.repository.PersonaRepository;
import ar.com.blschool.repository.UsuarioRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/perfil")
public class PerfilController {

    private final UsuarioRepository usuarioRepository;
    private final PersonaRepository personaRepository;
    private final AlumnoRepository alumnoRepository;
    private final ComisionRepository comisionRepository;
    private final CursoRepository cursoRepository;

    public PerfilController(UsuarioRepository usuarioRepository, PersonaRepository personaRepository,
                            AlumnoRepository alumnoRepository, ComisionRepository comisionRepository,
                            CursoRepository cursoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.personaRepository = personaRepository;
        this.alumnoRepository = alumnoRepository;
        this.comisionRepository = comisionRepository;
        this.cursoRepository = cursoRepository;
    }

    @GetMapping
    public PerfilDTO obtenerPerfil() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Usuario usuario = usuarioRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        PerfilDTO dto = new PerfilDTO();
        dto.setUsername(usuario.getUsername());
        dto.setRoles(usuario.getRoles().stream().map(Rol::getNombre).toList());

        if (usuario.getPersonaId() != null) {
            personaRepository.findById(usuario.getPersonaId()).ifPresent(persona -> {
                dto.setPersonaId(persona.getPerId());
                dto.setNombres(persona.getPerNombres());
                dto.setApellidos(persona.getPerApellido());
                dto.setEmail(persona.getPerEmail());
            });
        }

        return dto;
    }

    @GetMapping("/dashboard")
    public DashboardDTO obtenerDashboard() {
        DashboardDTO dto = new DashboardDTO();
        dto.setAlumnosActivos(alumnoRepository.countByAluEstado("ACTIVO"));
        dto.setDocentesActivos(usuarioRepository.countActivosByRol("DOCENTE"));
        dto.setCursosActivos(cursoRepository.countCursosConComisionActiva());
        dto.setComisionesActivas(comisionRepository.countByComActivaTrue());
        return dto;
    }
}
