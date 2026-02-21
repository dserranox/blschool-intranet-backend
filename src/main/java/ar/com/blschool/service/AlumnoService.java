package ar.com.blschool.service;

import ar.com.blschool.dto.AlumnoDTO;
import ar.com.blschool.dto.PersonaTelefonoDTO;
import ar.com.blschool.entity.Alumno;
import ar.com.blschool.entity.Inscripcion;
import ar.com.blschool.entity.Persona;
import ar.com.blschool.entity.PersonaTelefono;
import ar.com.blschool.entity.Comision;
import ar.com.blschool.repository.AlumnoRepository;
import ar.com.blschool.repository.ComisionRepository;
import ar.com.blschool.repository.InscripcionRepository;
import ar.com.blschool.repository.PersonaRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AlumnoService extends BaseService {

    private final AlumnoRepository alumnoRepository;
    private final PersonaRepository personaRepository;
    private final InscripcionRepository inscripcionRepository;
    private final ComisionRepository comisionRepository;

    private final String ALUMNO_INSCRIPTO = "INSCRIPTO";
    public AlumnoService(AlumnoRepository alumnoRepository,
                         PersonaRepository personaRepository,
                         InscripcionRepository inscripcionRepository,
                         ComisionRepository comisionRepository) {
        this.alumnoRepository = alumnoRepository;
        this.personaRepository = personaRepository;
        this.inscripcionRepository = inscripcionRepository;
        this.comisionRepository = comisionRepository;
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

    @Transactional(readOnly = true)
    public AlumnoDTO obtenerPorId(Long id) {
        Alumno alumno = alumnoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alumno no encontrado con id: " + id));
        return toDTO(alumno);
    }

    @Transactional
    public AlumnoDTO crear(AlumnoDTO dto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // 1. Crear Persona
        Persona persona = new Persona();
        persona.setPerApellido(dto.getApellidos());
        persona.setPerNombres(dto.getNombres());
        persona.setPerDni(dto.getDni());
        persona.setPerFechaNacimiento(dto.getFechaNacimiento());
        persona.setPerEmail(dto.getEmail());
        persona.setPerDireccion(dto.getDireccion());
        persona.setPersonasTelefonos(new ArrayList<>());
        auditar(persona, username);
        personaRepository.save(persona);

        // 2. Crear Alumno
        Alumno alumno = new Alumno();
        alumno.setPersona(persona);
        alumno.setAluEscuela(dto.getEscuela());
        alumno.setAluGradoCurso(dto.getGradoCurso());
        alumno.setAluEmailAlternativo(dto.getEmailAlternativo());
        alumno.setAluEstado(dto.getEstado() != null ? dto.getEstado() : ALUMNO_INSCRIPTO);
        auditar(alumno, username);
        alumnoRepository.save(alumno);

        // 3. Guardar teléfonos
        guardarTelefonos(persona, dto.getTelefonos(), username);

        // 4. Guardar inscripción si se seleccionó comisión
        guardarInscripcion(alumno, dto.getComisionId(), username);

        dto.setId(alumno.getAluId());
        return dto;
    }

    @Transactional
    public AlumnoDTO modificar(Long id, AlumnoDTO dto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Alumno alumno = alumnoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alumno no encontrado con id: " + id));

        // 1. Actualizar Persona
        Persona persona = alumno.getPersona();
        persona.setPerApellido(dto.getApellidos());
        persona.setPerNombres(dto.getNombres());
        persona.setPerDni(dto.getDni());
        persona.setPerFechaNacimiento(dto.getFechaNacimiento());
        persona.setPerEmail(dto.getEmail());
        persona.setPerDireccion(dto.getDireccion());
        auditar(persona, username);
        personaRepository.save(persona);

        // 2. Actualizar Alumno
        alumno.setAluEscuela(dto.getEscuela());
        alumno.setAluGradoCurso(dto.getGradoCurso());
        alumno.setAluEmailAlternativo(dto.getEmailAlternativo());
        if (dto.getEstado() != null) {
            alumno.setAluEstado(dto.getEstado());
        }
        auditar(alumno, username);
        alumnoRepository.save(alumno);

        // 3. Guardar teléfonos
        guardarTelefonos(persona, dto.getTelefonos(), username);

        // 4. Guardar inscripción si se seleccionó comisión
        guardarInscripcion(alumno, dto.getComisionId(), username);

        dto.setId(id);
        return dto;
    }

    @Transactional
    public void darDeBaja(Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Alumno alumno = alumnoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alumno no encontrado con id: " + id));

        alumno.setAluEstado("INACTIVO");
        auditar(alumno, username);
        alumnoRepository.save(alumno);

        List<Inscripcion> inscripcionesActivas = inscripcionRepository.findActivasByAlumnoId(id);
        for (Inscripcion inscripcion : inscripcionesActivas) {
            inscripcion.setInsEstado("INACTIVA");
            inscripcionRepository.save(inscripcion);
        }
    }

    private void guardarTelefonos(Persona persona, List<PersonaTelefonoDTO> telefonosDTO, String username) {
        if (telefonosDTO == null) {
            return;
        }

        List<PersonaTelefono> telefonosActuales = persona.getPersonasTelefonos();
        if (telefonosActuales == null) {
            telefonosActuales = new ArrayList<>();
            persona.setPersonasTelefonos(telefonosActuales);
        }

        // IDs que vienen del frontend (los que tienen id son existentes)
        Set<Long> idsRecibidos = telefonosDTO.stream()
                .map(PersonaTelefonoDTO::getId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        // Eliminar los que ya no están en la lista del frontend
        telefonosActuales.removeIf(tel -> !idsRecibidos.contains(tel.getPteId()));

        for (PersonaTelefonoDTO telDTO : telefonosDTO) {
            if (telDTO.getId() != null) {
                // Actualizar existente
                telefonosActuales.stream()
                        .filter(t -> t.getPteId().equals(telDTO.getId()))
                        .findFirst()
                        .ifPresent(tel -> {
                            tel.setPteNumero(telDTO.getNumero());
                            tel.setPteTipo(telDTO.getTipo());
                            tel.setPteNota(telDTO.getNota());
                            tel.setPtePrincipal(telDTO.getPrincipal() != null && telDTO.getPrincipal());
                            auditar(tel, username);
                        });
            } else {
                // Crear nuevo
                PersonaTelefono nuevo = new PersonaTelefono();
                nuevo.setPersona(persona);
                nuevo.setPteNumero(telDTO.getNumero());
                nuevo.setPteTipo(telDTO.getTipo());
                nuevo.setPteNota(telDTO.getNota());
                nuevo.setPtePrincipal(telDTO.getPrincipal() != null && telDTO.getPrincipal());
                auditar(nuevo, username);
                telefonosActuales.add(nuevo);
            }
        }

        personaRepository.save(persona);
    }

    private void guardarInscripcion(Alumno alumno, Long comisionId, String username) {
        if (comisionId == null) {
            return;
        }

        List<Inscripcion> inscripcionesActivas = inscripcionRepository.findActivasByAlumnoId(alumno.getAluId());

        if (!inscripcionesActivas.isEmpty()) {
            Inscripcion inscripcionExistente = inscripcionesActivas.getFirst();
            // Si la comisión es la misma, no hace nada
            if (!inscripcionExistente.getComision().getComId().equals(comisionId)) {
                // Actualizar la inscripción existente con la nueva comisión
                Comision nuevaComision = comisionRepository.findById(comisionId)
                        .orElseThrow(() -> new RuntimeException("Comisión no encontrada con id: " + comisionId));
                inscripcionExistente.setComision(nuevaComision);
            }
            inscripcionExistente.setInsFecha(LocalDateTime.now());
            inscripcionExistente.setInsEstado(alumno.getAluEstado().equals(ALUMNO_INSCRIPTO) ? "ACTIVA" : "INACTIVA");
            inscripcionRepository.save(inscripcionExistente);
        } else {
            // Crear nueva inscripción
            Comision comision = comisionRepository.findById(comisionId)
                    .orElseThrow(() -> new RuntimeException("Comisión no encontrada con id: " + comisionId));
            Inscripcion inscripcion = new Inscripcion();
            inscripcion.setAlumno(alumno);
            inscripcion.setComision(comision);
            inscripcion.setInsFecha(LocalDateTime.now());
            inscripcion.setInsEstado(alumno.getAluEstado().equals(ALUMNO_INSCRIPTO) ? "ACTIVA" : "INACTIVA");
            inscripcionRepository.save(inscripcion);
        }
    }

    private AlumnoDTO toDTO(Alumno alumno) {
        AlumnoDTO dto = new AlumnoDTO();
        Persona persona = alumno.getPersona();

        dto.setId(alumno.getAluId());
        dto.setApellidos(persona.getPerApellido());
        dto.setNombres(persona.getPerNombres());
        dto.setDni(persona.getPerDni());
        dto.setFechaNacimiento(persona.getPerFechaNacimiento());
        dto.setEmail(persona.getPerEmail());
        dto.setDireccion(persona.getPerDireccion());
        dto.setEmailAlternativo(alumno.getAluEmailAlternativo());
        dto.setEscuela(alumno.getAluEscuela());
        dto.setGradoCurso(alumno.getAluGradoCurso());
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
            dto.setComisionId(inscripcion.getComision().getComId());
            dto.setComision(inscripcion.getComision().getComNombre());
            dto.setCurso(inscripcion.getComision().getCurso().getCurNombre());
        }

        return dto;
    }
}
