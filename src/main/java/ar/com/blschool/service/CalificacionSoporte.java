package ar.com.blschool.service;

import ar.com.blschool.dto.FilaAlumnoDTO;
import ar.com.blschool.entity.*;
import ar.com.blschool.repository.AlumnoRepository;
import ar.com.blschool.repository.ComisionRepository;
import ar.com.blschool.repository.InscripcionRepository;
import ar.com.blschool.repository.LogroRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class CalificacionSoporte {

    /** Alumno de una planilla: editable si está inscripto (ACTIVA) en la comisión. */
    public record AlumnoPlanilla(Alumno alumno, boolean editable) {}

    private final ComisionRepository comisionRepository;
    private final InscripcionRepository inscripcionRepository;
    private final AlumnoRepository alumnoRepository;
    private final LogroRepository logroRepository;

    public CalificacionSoporte(ComisionRepository comisionRepository,
                               InscripcionRepository inscripcionRepository,
                               AlumnoRepository alumnoRepository,
                               LogroRepository logroRepository) {
        this.comisionRepository = comisionRepository;
        this.inscripcionRepository = inscripcionRepository;
        this.alumnoRepository = alumnoRepository;
        this.logroRepository = logroRepository;
    }

    /** Copia congelada de los logros del curso en la evaluación. 409 si el curso no tiene logros configurados. */
    public void copiarLogrosDelCurso(Evaluacion evaluacion) {
        Curso curso = evaluacion.getComision().getCurso();
        List<Logro> logrosCurso = logroRepository.findByCursoCurIdOrderByLogOrdenAsc(curso.getCurId());
        if (logrosCurso.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Configurá los logros de " + curso.getCurNombre() + " primero");
        }
        for (Logro logro : logrosCurso) {
            EvaluacionLogro copia = new EvaluacionLogro();
            copia.setEvaluacion(evaluacion);
            copia.setEloNombre(logro.getLogNombre());
            copia.setEloPeso(logro.getLogPeso());
            copia.setEloOrden(logro.getLogOrden());
            evaluacion.getLogros().add(copia);
        }
    }

    public Comision buscarComision(Long comisionId) {
        return comisionRepository.findById(comisionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comisión no encontrada con id: " + comisionId));
    }

    /** Inscriptos ACTIVA de la comisión, por id de alumno. */
    public Map<Long, Alumno> alumnosInscriptos(Long comisionId) {
        return inscripcionRepository.findByComisionId(comisionId).stream()
                .filter(i -> "ACTIVA".equals(i.getInsEstado()))
                .collect(Collectors.toMap(i -> i.getAlumno().getAluId(), Inscripcion::getAlumno, (a, b) -> a));
    }

    /** Inscriptos (editables) + alumnos que ya no están pero tienen notas (no editables), ordenados por apellido. */
    public List<AlumnoPlanilla> alumnosDePlanilla(Long comisionId, Collection<Long> alumnosConNotas) {
        Map<Long, AlumnoPlanilla> resultado = new LinkedHashMap<>();
        alumnosInscriptos(comisionId).forEach((id, alumno) -> resultado.put(id, new AlumnoPlanilla(alumno, true)));
        for (Long alumnoId : alumnosConNotas) {
            if (!resultado.containsKey(alumnoId)) {
                alumnoRepository.findById(alumnoId).ifPresent(a -> resultado.put(alumnoId, new AlumnoPlanilla(a, false)));
            }
        }
        return resultado.values().stream()
                .sorted(Comparator.comparing((AlumnoPlanilla a) -> a.alumno().getPersona().getPerApellido(), String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(a -> a.alumno().getPersona().getPerNombres(), String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    /** 400 si el alumno no está inscripto (sólo se cargan notas a inscriptos activos). */
    public Alumno alumnoEditable(Map<Long, Alumno> inscriptos, Long alumnoId) {
        Alumno alumno = inscriptos.get(alumnoId);
        if (alumno == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El alumno " + alumnoId + " no está inscripto en la comisión");
        }
        return alumno;
    }

    public void completarFila(FilaAlumnoDTO fila, AlumnoPlanilla alumno) {
        Persona persona = alumno.alumno().getPersona();
        fila.setAlumnoId(alumno.alumno().getAluId());
        fila.setApellidos(persona.getPerApellido());
        fila.setNombres(persona.getPerNombres());
        fila.setEditable(alumno.editable());
    }

    public String nombre(Alumno alumno) {
        return alumno.getPersona().getPerApellido() + ", " + alumno.getPersona().getPerNombres();
    }

    public Calificacion nuevaCalificacion(Evaluacion evaluacion, Alumno alumno) {
        Calificacion calificacion = new Calificacion();
        calificacion.setEvaluacion(evaluacion);
        calificacion.setAlumno(alumno);
        calificacion.setCalAusente(false);
        return calificacion;
    }

    public String usuarioActual() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
