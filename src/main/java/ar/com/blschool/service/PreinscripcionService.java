package ar.com.blschool.service;

import ar.com.blschool.dto.PreinscripcionDTO;
import ar.com.blschool.dto.PreinscripcionFormDTO;
import ar.com.blschool.dto.PreinscripcionResultadoDTO;
import ar.com.blschool.entity.*;
import ar.com.blschool.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ar.com.blschool.util.FormularioNormalizer.*;

@Service
public class PreinscripcionService extends BaseService {

    private static final String USUARIO_FORMULARIO = "GOOGLE_FORM";
    private static final String ORIGEN_FORMULARIO = "GOOGLE_FORM";

    private static final String ALUMNO_INSCRIPTO = "INSCRIPTO";
    private static final String ALUMNO_PREINSCRIPTO = "PREINSCRIPTO";
    private static final String ALUMNO_INACTIVO = "INACTIVO";

    private final PersonaRepository personaRepository;
    private final AlumnoRepository alumnoRepository;
    private final PreinscripcionRepository preinscripcionRepository;
    private final InscripcionRepository inscripcionRepository;
    private final ComisionRepository comisionRepository;
    private final ObjectMapper objectMapper;

    public PreinscripcionService(PersonaRepository personaRepository,
                                 AlumnoRepository alumnoRepository,
                                 PreinscripcionRepository preinscripcionRepository,
                                 InscripcionRepository inscripcionRepository,
                                 ComisionRepository comisionRepository,
                                 ObjectMapper objectMapper) {
        this.personaRepository = personaRepository;
        this.alumnoRepository = alumnoRepository;
        this.preinscripcionRepository = preinscripcionRepository;
        this.inscripcionRepository = inscripcionRepository;
        this.comisionRepository = comisionRepository;
        this.objectMapper = objectMapper;
    }

    // =====================================================================
    // Alta / actualización desde el Google Form
    // =====================================================================

    /**
     * Procesa una respuesta del formulario. Es idempotente: reenviar la misma respuesta
     * (o una nueva del mismo alumno para el mismo año) actualiza en lugar de duplicar.
     * Nunca toca las inscripciones existentes del alumno.
     */
    @Transactional
    public PreinscripcionResultadoDTO procesarFormulario(PreinscripcionFormDTO form) {
        String dni = normalizarDni(form.getDni());
        if (dni == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "DNI inválido o vacío: '" + form.getDni() + "'");
        }

        LocalDateTime fechaFormulario = parsearFechaHora(form.getMarcaTemporal());
        int anio = form.getAnio() != null
                ? form.getAnio()
                : (fechaFormulario != null ? fechaFormulario.getYear() : LocalDate.now().getYear()) + 1;

        Persona persona = personaRepository.findByPerDni(dni).orElse(null);
        Alumno alumno = persona != null ? alumnoRepository.findById(persona.getPerId()).orElse(null) : null;
        boolean alumnoNuevo = alumno == null;

        if (persona == null) {
            persona = crearPersona(form, dni);
        } else {
            actualizarPersona(persona, form);
        }

        if (alumno == null) {
            alumno = new Alumno();
            alumno.setPersona(persona);
            alumno.setAluEstado(ALUMNO_PREINSCRIPTO);
        } else if (alumno.getAluEstado() == null || ALUMNO_INACTIVO.equals(alumno.getAluEstado())) {
            // Un alumno dado de baja que vuelve a anotarse pasa a preinscripto.
            // Si está INSCRIPTO en el año en curso no se toca su estado.
            alumno.setAluEstado(ALUMNO_PREINSCRIPTO);
        }
        actualizarAlumno(alumno, form);
        auditar(alumno, USUARIO_FORMULARIO);
        alumnoRepository.save(alumno);

        actualizarTelefono(persona, "MAMA", form.getTelefonoMadre());
        actualizarTelefono(persona, "PAPA", form.getTelefonoPadre());
        actualizarTelefono(persona, "ALUMNO", form.getTelefonoAlumno());
        personaRepository.save(persona);

        Preinscripcion preinscripcion = preinscripcionRepository
                .findByAlumnoAluIdAndPreAnio(alumno.getAluId(), anio)
                .orElse(null);
        boolean preinscripcionNueva = preinscripcion == null;
        if (preinscripcionNueva) {
            preinscripcion = new Preinscripcion();
            preinscripcion.setAlumno(alumno);
            preinscripcion.setPreAnio(anio);
            preinscripcion.setPreAlumnoNuevo(alumnoNuevo);
            preinscripcion.setPreEstado(Preinscripcion.PENDIENTE);
            preinscripcion.setPreOrigen(ORIGEN_FORMULARIO);
        } else if (Preinscripcion.DESCARTADA.equals(preinscripcion.getPreEstado())) {
            // Volvió a mandar el formulario después de haber sido descartada: vuelve a quedar pendiente
            preinscripcion.setPreEstado(Preinscripcion.PENDIENTE);
        }
        if (fechaFormulario != null || preinscripcion.getPreFechaFormulario() == null) {
            preinscripcion.setPreFechaFormulario(fechaFormulario != null ? fechaFormulario : LocalDateTime.now());
        }
        String escuelaCursoTurno = limpiar(form.getEscuelaCursoTurno());
        if (escuelaCursoTurno != null) {
            preinscripcion.setPreEscuelaCursoTurno(truncar(escuelaCursoTurno, 300));
        }
        preinscripcion.setPreDatosFormulario(objectMapper.writeValueAsString(form));
        auditar(preinscripcion, USUARIO_FORMULARIO);
        preinscripcionRepository.save(preinscripcion);

        return new PreinscripcionResultadoDTO(preinscripcion.getPreId(), alumno.getAluId(), anio,
                alumnoNuevo, preinscripcionNueva);
    }

    private Persona crearPersona(PreinscripcionFormDTO form, String dni) {
        String nombre = limpiar(form.getNombre());
        String apellido = limpiar(form.getApellido());

        Persona persona = new Persona();
        persona.setPerNombres(truncar(nombre != null ? nombre : "", 150));
        persona.setPerApellido(truncar(apellido != null ? apellido : "", 150));
        persona.setPerDni(dni);
        persona.setPerFechaNacimiento(parsearFecha(form.getFechaNacimiento()));
        persona.setPerEmail(truncar(email(form), 200));
        persona.setPerDireccion(truncar(limpiar(form.getDomicilio()), 250));
        persona.setPersonasTelefonos(new ArrayList<>());
        auditar(persona, USUARIO_FORMULARIO);
        return personaRepository.save(persona);
    }

    /** Alumno existente: sólo se actualizan los datos que pueden cambiar y que vienen informados. */
    private void actualizarPersona(Persona persona, PreinscripcionFormDTO form) {
        String domicilio = limpiar(form.getDomicilio());
        if (domicilio != null) {
            persona.setPerDireccion(truncar(domicilio, 250));
        }
        String email = email(form);
        if (email != null) {
            persona.setPerEmail(truncar(email, 200));
        }
        if (persona.getPersonasTelefonos() == null) {
            persona.setPersonasTelefonos(new ArrayList<>());
        }
        auditar(persona, USUARIO_FORMULARIO);
    }

    private void actualizarAlumno(Alumno alumno, PreinscripcionFormDTO form) {
        String escuela = limpiar(form.getEscuelaCursoTurno());
        if (escuela != null) {
            alumno.setAluEscuela(truncar(escuela, 200));
        }

        Boolean tieneProblemaSalud = parsearSiNo(form.getTieneProblemaSalud());
        String detalle = limpiar(form.getProblemaSalud());
        if (Boolean.TRUE.equals(tieneProblemaSalud)) {
            alumno.setAluProblemaSalud(truncar(detalle != null ? detalle : "Sí (sin especificar en el formulario)", 500));
        } else if (Boolean.FALSE.equals(tieneProblemaSalud)) {
            alumno.setAluProblemaSalud(null);
        } else if (detalle != null) {
            alumno.setAluProblemaSalud(truncar(detalle, 500));
        }
    }

    /**
     * Si el número ya está cargado (con cualquier tipo) no hace nada.
     * Si existe un teléfono de ese tipo, lo reemplaza por el nuevo número; si no, lo agrega.
     */
    private void actualizarTelefono(Persona persona, String tipo, String numeroFormulario) {
        String numero = normalizarTelefono(numeroFormulario);
        if (numero == null) {
            return;
        }
        List<PersonaTelefono> telefonos = persona.getPersonasTelefonos();

        boolean yaExiste = telefonos.stream()
                .anyMatch(t -> numero.equals(soloDigitos(t.getPteNumero())));
        if (yaExiste) {
            return;
        }

        PersonaTelefono existente = telefonos.stream()
                .filter(t -> tipo.equals(t.getPteTipo()))
                .findFirst()
                .orElse(null);
        if (existente != null) {
            existente.setPteNumero(numero);
            auditar(existente, USUARIO_FORMULARIO);
            return;
        }

        PersonaTelefono nuevo = new PersonaTelefono();
        nuevo.setPersona(persona);
        nuevo.setPteNumero(numero);
        nuevo.setPteTipo(tipo);
        nuevo.setPtePrincipal(telefonos.stream().noneMatch(t -> Boolean.TRUE.equals(t.getPtePrincipal())));
        auditar(nuevo, USUARIO_FORMULARIO);
        telefonos.add(nuevo);
    }

    private String email(PreinscripcionFormDTO form) {
        String email = limpiar(form.getEmail());
        return email != null ? email : limpiar(form.getEmailFormulario());
    }

    // =====================================================================
    // Gestión por el administrador
    // =====================================================================

    @Transactional(readOnly = true)
    public List<PreinscripcionDTO> listar(Integer anio, String estado) {
        Map<Long, Inscripcion> inscripcionAnterior = inscripcionRepository.findActivasByAnio(anio - 1).stream()
                .collect(Collectors.toMap(i -> i.getAlumno().getAluId(), Function.identity(), (a, b) -> a));

        return preinscripcionRepository.findByAnioWithAlumno(anio).stream()
                .filter(p -> estado == null || estado.isBlank() || "TODOS".equalsIgnoreCase(estado)
                        || estado.equalsIgnoreCase(p.getPreEstado()))
                .map(p -> {
                    PreinscripcionDTO dto = toDTO(p);
                    Inscripcion anterior = inscripcionAnterior.get(p.getAlumno().getAluId());
                    if (anterior != null) {
                        dto.setComisionAnterior(anterior.getComision().getComNombre());
                        dto.setCursoAnterior(anterior.getComision().getCurso().getCurNombre());
                    }
                    return dto;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PreinscripcionDTO> listarPorAlumno(Long alumnoId) {
        return preinscripcionRepository.findByAlumnoId(alumnoId).stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Integer> obtenerAnios() {
        return preinscripcionRepository.findDistinctAnios();
    }

    /**
     * Asigna la preinscripción a una comisión del mismo año: crea (o mueve, si ya estaba asignada)
     * la inscripción del alumno para ese año. Las inscripciones de otros años no se tocan.
     */
    @Transactional
    public PreinscripcionDTO asignarComision(Long preinscripcionId, Long comisionId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Preinscripcion preinscripcion = buscar(preinscripcionId);
        Comision comision = comisionRepository.findById(comisionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comisión no encontrada con id: " + comisionId));
        if (!comision.getComAnio().equals(preinscripcion.getPreAnio())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La comisión es del año " + comision.getComAnio() + " y la preinscripción del " + preinscripcion.getPreAnio());
        }

        Alumno alumno = preinscripcion.getAlumno();

        // Reutilizar la inscripción de ese año si ya existe (asignada antes o cargada a mano desde el alumno)
        Inscripcion inscripcion = preinscripcion.getInscripcion();
        if (inscripcion == null) {
            inscripcion = inscripcionRepository.findHistorialByAlumnoId(alumno.getAluId()).stream()
                    .filter(i -> i.getComision().getComAnio().equals(preinscripcion.getPreAnio()))
                    .findFirst()
                    .orElse(null);
        }
        if (inscripcion == null) {
            inscripcion = new Inscripcion();
            inscripcion.setAlumno(alumno);
        }
        if (inscripcion.getInsId() == null || !comisionId.equals(inscripcion.getComision().getComId())) {
            inscripcion.setInsFecha(LocalDateTime.now());
        }
        inscripcion.setComision(comision);
        inscripcion.setInsEstado("ACTIVA");
        inscripcionRepository.save(inscripcion);

        if (!ALUMNO_INSCRIPTO.equals(alumno.getAluEstado())) {
            alumno.setAluEstado(ALUMNO_INSCRIPTO);
            auditar(alumno, username);
            alumnoRepository.save(alumno);
        }

        preinscripcion.setComision(comision);
        preinscripcion.setInscripcion(inscripcion);
        preinscripcion.setPreEstado(Preinscripcion.ASIGNADA);
        auditar(preinscripcion, username);
        preinscripcionRepository.save(preinscripcion);

        return toDTO(preinscripcion);
    }

    /** Descarta la preinscripción. Si ya tenía comisión asignada, esa inscripción queda INACTIVA. */
    @Transactional
    public PreinscripcionDTO descartar(Long preinscripcionId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Preinscripcion preinscripcion = buscar(preinscripcionId);
        if (preinscripcion.getInscripcion() != null) {
            preinscripcion.getInscripcion().setInsEstado("INACTIVA");
            inscripcionRepository.save(preinscripcion.getInscripcion());
        }

        Alumno alumno = preinscripcion.getAlumno();
        if (ALUMNO_PREINSCRIPTO.equals(alumno.getAluEstado())) {
            alumno.setAluEstado(ALUMNO_INACTIVO);
            auditar(alumno, username);
            alumnoRepository.save(alumno);
        }

        preinscripcion.setPreEstado(Preinscripcion.DESCARTADA);
        auditar(preinscripcion, username);
        preinscripcionRepository.save(preinscripcion);

        return toDTO(preinscripcion);
    }

    private Preinscripcion buscar(Long id) {
        return preinscripcionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Preinscripción no encontrada con id: " + id));
    }

    private PreinscripcionDTO toDTO(Preinscripcion p) {
        PreinscripcionDTO dto = new PreinscripcionDTO();
        dto.setId(p.getPreId());
        dto.setAnio(p.getPreAnio());
        dto.setEstado(p.getPreEstado());
        dto.setOrigen(p.getPreOrigen());
        dto.setFechaFormulario(p.getPreFechaFormulario());
        dto.setEscuelaCursoTurno(p.getPreEscuelaCursoTurno());
        dto.setAlumnoNuevo(p.getPreAlumnoNuevo());

        Alumno alumno = p.getAlumno();
        Persona persona = alumno.getPersona();
        dto.setAlumnoId(alumno.getAluId());
        dto.setApellidos(persona.getPerApellido());
        dto.setNombres(persona.getPerNombres());
        dto.setDni(persona.getPerDni());
        dto.setAlumnoEstado(alumno.getAluEstado());

        if (p.getComision() != null) {
            dto.setComisionId(p.getComision().getComId());
            dto.setComision(p.getComision().getComNombre());
            dto.setCurso(p.getComision().getCurso().getCurNombre());
        }
        return dto;
    }
}
