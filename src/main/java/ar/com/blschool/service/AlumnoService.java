package ar.com.blschool.service;

import ar.com.blschool.dto.AlumnoDTO;
import ar.com.blschool.dto.PersonaTelefonoDTO;
import ar.com.blschool.entity.Alumno;
import ar.com.blschool.entity.Inscripcion;
import ar.com.blschool.entity.Persona;
import ar.com.blschool.repository.AlumnoRepository;
import ar.com.blschool.repository.InscripcionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
public class AlumnoService extends BaseService {

    private final AlumnoRepository alumnoRepository;
    private final InscripcionRepository inscripcionRepository;

    public AlumnoService(AlumnoRepository alumnoRepository, InscripcionRepository inscripcionRepository) {
        this.alumnoRepository = alumnoRepository;
        this.inscripcionRepository = inscripcionRepository;
    }

    @Transactional(readOnly = true)
    public List<AlumnoDTO> listar(String estado) {
        List<Alumno> alumnos;
        if (estado == null || estado.isBlank() || "TODOS".equalsIgnoreCase(estado)) {
            alumnos = alumnoRepository.findAllWithPersona();
        } else {
            alumnos = alumnoRepository.findByEstadoWithPersona(estado);
        }
        return alumnos.stream().map(this::toDTO).toList();
    }

    private AlumnoDTO toDTO(Alumno alumno) {
        AlumnoDTO dto = new AlumnoDTO();
        Persona persona = alumno.getPersona();

        dto.setId(alumno.getAluId());
        dto.setApellidos(persona.getPerApellido());
        dto.setNombres(persona.getPerNombres());
        dto.setDni(persona.getPerDni());
        dto.setFechaNacimiento(persona.getPerFechaNacimiento());
        dto.setEstado(alumno.getAluEstado());

        if (persona.getPersonasTelefonos() != null) {
            dto.setTelefonos(persona.getPersonasTelefonos().stream()
                    .map(PersonaTelefonoDTO::new)
                    .toList());
        } else {
            dto.setTelefonos(Collections.emptyList());
        }

        List<Inscripcion> inscripcionesActivas = inscripcionRepository.findActivasByAlumnoId(alumno.getAluId());
        if (!inscripcionesActivas.isEmpty()) {
            Inscripcion inscripcion = inscripcionesActivas.getFirst();
            dto.setComision(inscripcion.getComision().getComNombre());
            dto.setCurso(inscripcion.getComision().getCurso().getCurNombre());
        }

        return dto;
    }
}
