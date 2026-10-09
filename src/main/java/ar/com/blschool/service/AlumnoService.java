package ar.com.blschool.service;

import ar.com.blschool.dto.AlumnoDTO;
import ar.com.blschool.dto.InscripcionHistorialDTO;
import ar.com.blschool.dto.PersonaTelefonoDTO;
import ar.com.blschool.entity.Alumno;
import ar.com.blschool.entity.Inscripcion;
import ar.com.blschool.entity.Persona;
import ar.com.blschool.entity.PersonaTelefono;
import ar.com.blschool.entity.Comision;
import ar.com.blschool.entity.Preinscripcion;
import ar.com.blschool.repository.AlumnoRepository;
import ar.com.blschool.repository.ComisionRepository;
import ar.com.blschool.repository.InscripcionRepository;
import ar.com.blschool.repository.PersonaRepository;
import ar.com.blschool.repository.PreinscripcionRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AlumnoService extends BaseService {

    private final AlumnoRepository alumnoRepository;
    private final PersonaRepository personaRepository;
    private final InscripcionRepository inscripcionRepository;
    private final ComisionRepository comisionRepository;
    private final PreinscripcionRepository preinscripcionRepository;

    private final String ALUMNO_INSCRIPTO = "INSCRIPTO";
    public AlumnoService(AlumnoRepository alumnoRepository,
                         PersonaRepository personaRepository,
                         InscripcionRepository inscripcionRepository,
                         ComisionRepository comisionRepository,
                         PreinscripcionRepository preinscripcionRepository) {
        this.alumnoRepository = alumnoRepository;
        this.personaRepository = personaRepository;
        this.inscripcionRepository = inscripcionRepository;
        this.comisionRepository = comisionRepository;
        this.preinscripcionRepository = preinscripcionRepository;
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

    @Transactional(readOnly = true)
    public List<AlumnoDTO> listarPorComision(Long comisionId) {
        return inscripcionRepository.findByComisionId(comisionId).stream()
                .map(inscripcion -> {
                    AlumnoDTO dto = toDTO(inscripcion.getAlumno());
                    return dto;
                })
                .toList();
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
        alumno.setAluProblemaSalud(dto.getProblemaSalud());
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
        alumno.setAluProblemaSalud(dto.getProblemaSalud());
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

    /**
     * Las inscripciones son por año: elegir una comisión sólo reemplaza la inscripción del MISMO año
     * de esa comisión. Las de otros años (historial, o la del año siguiente) no se tocan.
     */
    private void guardarInscripcion(Alumno alumno, Long comisionId, String username) {
        List<Inscripcion> inscripcionesActivas = inscripcionRepository.findActivasByAlumnoId(alumno.getAluId());

        if (comisionId == null) {
            // "Sin comisión": se quita sólo la inscripción que muestra el formulario
            Inscripcion vigente = inscripcionVigente(inscripcionesActivas);
            if (vigente != null) {
                preinscripcionRepository.findByInscripcionInsId(vigente.getInsId()).forEach(p -> {
                    p.setInscripcion(null);
                    p.setComision(null);
                    p.setPreEstado(Preinscripcion.PENDIENTE);
                    auditar(p, username);
                    preinscripcionRepository.save(p);
                });
                inscripcionRepository.delete(vigente);
            }
            return;
        }

        Comision comision = comisionRepository.findById(comisionId)
                .orElseThrow(() -> new RuntimeException("Comisión no encontrada con id: " + comisionId));

        Inscripcion inscripcionExistente = inscripcionesActivas.stream()
                .filter(i -> i.getComision().getComAnio().equals(comision.getComAnio()))
                .findFirst()
                .orElse(null);

        if (inscripcionExistente != null) {
            if (!inscripcionExistente.getComision().getComId().equals(comisionId)) {
                // Cambio de comisión dentro del mismo año
                inscripcionExistente.setComision(comision);
                inscripcionExistente.setInsFecha(LocalDateTime.now());
            }
            inscripcionExistente.setInsEstado(alumno.getAluEstado().equals(ALUMNO_INSCRIPTO) ? "ACTIVA" : "INACTIVA");
            inscripcionRepository.save(inscripcionExistente);
        } else {
            // Crear nueva inscripción (otro año, o el alumno no tenía)
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
        dto.setProblemaSalud(alumno.getAluProblemaSalud());

        if (persona.getPersonasTelefonos() != null) {
            dto.setTelefonos(persona.getPersonasTelefonos().stream()
                    .map(PersonaTelefonoDTO::new)
                    .toList());
        } else {
            dto.setTelefonos(Collections.emptyList());
        }

        Inscripcion inscripcion = inscripcionVigente(inscripcionRepository.findActivasByAlumnoId(alumno.getAluId()));
        if (inscripcion != null) {
            dto.setComisionId(inscripcion.getComision().getComId());
            dto.setComision(inscripcion.getComision().getComNombre());
            dto.setComisionAnio(inscripcion.getComision().getComAnio());
            dto.setCurso(inscripcion.getComision().getCurso().getCurNombre());
        }

        return dto;
    }

    /**
     * Inscripción a mostrar como "la comisión del alumno": la del año en curso si existe;
     * si no, la del año más reciente (p. ej. un alumno nuevo que sólo tiene la del año siguiente).
     */
    private Inscripcion inscripcionVigente(List<Inscripcion> inscripcionesActivas) {
        int anioActual = LocalDate.now().getYear();
        return inscripcionesActivas.stream()
                .filter(i -> i.getComision().getComAnio() == anioActual)
                .findFirst()
                .or(() -> inscripcionesActivas.stream()
                        .max(Comparator.comparing((Inscripcion i) -> i.getComision().getComAnio())
                                .thenComparing(Inscripcion::getInsId)))
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<InscripcionHistorialDTO> historialInscripciones(Long alumnoId) {
        return inscripcionRepository.findHistorialByAlumnoId(alumnoId).stream()
                .map(i -> {
                    InscripcionHistorialDTO dto = new InscripcionHistorialDTO();
                    dto.setId(i.getInsId());
                    dto.setAnio(i.getComision().getComAnio());
                    dto.setComisionId(i.getComision().getComId());
                    dto.setComision(i.getComision().getComNombre());
                    dto.setCurso(i.getComision().getCurso().getCurNombre());
                    dto.setEstado(i.getInsEstado());
                    dto.setFecha(i.getInsFecha());
                    return dto;
                })
                .toList();
    }
}
