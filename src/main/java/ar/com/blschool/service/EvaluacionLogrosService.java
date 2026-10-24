package ar.com.blschool.service;

import ar.com.blschool.dto.FilaLogrosDTO;
import ar.com.blschool.dto.GuardarFilaLogrosDTO;
import ar.com.blschool.dto.LogroDTO;
import ar.com.blschool.dto.PlanillaLogrosDTO;
import ar.com.blschool.entity.*;
import ar.com.blschool.repository.CalificacionRepository;
import ar.com.blschool.repository.EvaluacionRepository;
import ar.com.blschool.util.CalificacionReglas;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Planillas calificadas por logros: Parcial / Final de una comisión y, en los cursos Year/First,
 * cada prueba (que además admite Final Mark cargado a mano, excluyente con los logros).
 */
@Service
public class EvaluacionLogrosService extends BaseService {

    private final EvaluacionRepository evaluacionRepository;
    private final CalificacionRepository calificacionRepository;
    private final PermisoCalificacionService permisos;
    private final CalificacionSoporte soporte;

    public EvaluacionLogrosService(EvaluacionRepository evaluacionRepository,
                                   CalificacionRepository calificacionRepository,
                                   PermisoCalificacionService permisos,
                                   CalificacionSoporte soporte) {
        this.evaluacionRepository = evaluacionRepository;
        this.calificacionRepository = calificacionRepository;
        this.permisos = permisos;
        this.soporte = soporte;
    }

    /** Devuelve la planilla; la primera vez crea el Parcial/Final copiando los logros del curso. */
    @Transactional
    public PlanillaLogrosDTO obtenerPlanilla(Long comisionId, String tipo) {
        String tipoEvaluacion = validarTipo(tipo);
        permisos.verificarComision(comisionId);
        Comision comision = soporte.buscarComision(comisionId);
        Evaluacion evaluacion = evaluacionRepository.findByComisionComIdAndEvaTipo(comisionId, tipoEvaluacion)
                .orElseGet(() -> crearEvaluacion(comision, tipoEvaluacion));
        return armarPlanilla(comision, evaluacion);
    }

    /** Guarda las filas recibidas (sólo las modificadas). Todo-o-nada: una fila inválida -> 400 y no se guarda nada. */
    @Transactional
    public PlanillaLogrosDTO guardarPlanilla(Long comisionId, String tipo, List<GuardarFilaLogrosDTO> filas) {
        String tipoEvaluacion = validarTipo(tipo);
        permisos.verificarComision(comisionId);
        Comision comision = soporte.buscarComision(comisionId);
        Evaluacion evaluacion = evaluacionRepository.findByComisionComIdAndEvaTipo(comisionId, tipoEvaluacion)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "La evaluación no existe: abrí la planilla primero"));
        guardarFilas(evaluacion, filas);
        return armarPlanilla(comision, evaluacion);
    }

    /**
     * Planilla de una prueba de Year/First. Las pruebas creadas antes de este esquema no tienen logros:
     * se les copian los del curso al abrirlas y sus notas existentes quedan como Final Mark manual.
     */
    @Transactional
    public PlanillaLogrosDTO obtenerPlanillaPrueba(Long pruebaId) {
        Evaluacion prueba = buscarPruebaPorLogros(pruebaId);
        if (prueba.getLogros().isEmpty()) {
            soporte.copiarLogrosDelCurso(prueba);
            auditar(prueba, soporte.usuarioActual());
            prueba = evaluacionRepository.save(prueba);
        }
        return armarPlanilla(prueba.getComision(), prueba);
    }

    @Transactional
    public PlanillaLogrosDTO guardarPlanillaPrueba(Long pruebaId, List<GuardarFilaLogrosDTO> filas) {
        Evaluacion prueba = buscarPruebaPorLogros(pruebaId);
        if (prueba.getLogros().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La prueba no tiene logros: abrí la planilla primero");
        }
        guardarFilas(prueba, filas);
        return armarPlanilla(prueba.getComision(), prueba);
    }

    private void guardarFilas(Evaluacion evaluacion, List<GuardarFilaLogrosDTO> filas) {
        boolean admiteNotaManual = admiteNotaManual(evaluacion);
        Map<Long, EvaluacionLogro> logros = evaluacion.getLogros().stream()
                .collect(Collectors.toMap(EvaluacionLogro::getEloId, l -> l));
        Map<Long, Alumno> inscriptos = soporte.alumnosInscriptos(evaluacion.getComision().getComId());
        String username = soporte.usuarioActual();

        for (GuardarFilaLogrosDTO fila : filas) {
            if (fila == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fila vacía en la planilla");
            }
            Alumno alumno = soporte.alumnoEditable(inscriptos, fila.getAlumnoId());
            String nombreAlumno = soporte.nombre(alumno);
            boolean ausente = Boolean.TRUE.equals(fila.getAusente());
            BigDecimal notaManual = ausente ? null : fila.getNota();
            if (notaManual != null && !admiteNotaManual) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Esta evaluación no admite Final Mark manual");
            }
            Map<Long, BigDecimal> valores = ausente
                    ? new HashMap<>()
                    : valoresValidados(fila, logros, nombreAlumno);
            CalificacionReglas.validarNotaManual(nombreAlumno, notaManual, !valores.isEmpty());
            Calificacion calificacion = calificacionRepository
                    .findByEvaluacionEvaIdAndAlumnoAluId(evaluacion.getEvaId(), alumno.getAluId())
                    .orElse(null);

            if (!ausente && valores.isEmpty() && notaManual == null) {
                // Fila vacía = sin cargar
                if (calificacion != null) {
                    calificacionRepository.delete(calificacion);
                }
                continue;
            }
            if (calificacion == null) {
                calificacion = soporte.nuevaCalificacion(evaluacion, alumno);
            }
            calificacion.setCalAusente(ausente);
            if (ausente) {
                calificacion.getValores().clear();
                calificacion.setCalNota(null);
            } else if (notaManual != null) {
                calificacion.getValores().clear();
                calificacion.setCalNota(CalificacionReglas.redondear(notaManual));
            } else {
                sincronizarValores(calificacion, valores, logros);
                calificacion.setCalNota(CalificacionReglas.calcularNota(logros.keySet(), valores));
            }
            auditar(calificacion, username);
            calificacionRepository.save(calificacion);
        }
    }

    private String validarTipo(String tipo) {
        String valor = tipo == null ? "" : tipo.toUpperCase();
        if (!Evaluacion.PARCIAL.equals(valor) && !Evaluacion.FINAL.equals(valor)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de evaluación inválido: " + tipo);
        }
        return valor;
    }

    /** Prueba de un curso Year/First a la que el usuario tiene acceso. */
    private Evaluacion buscarPruebaPorLogros(Long pruebaId) {
        Evaluacion prueba = evaluacionRepository.findById(pruebaId)
                .filter(e -> Evaluacion.PRUEBA.equals(e.getEvaTipo()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prueba no encontrada con id: " + pruebaId));
        permisos.verificarComision(prueba.getComision().getComId());
        if (!admiteNotaManual(prueba)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Las pruebas de este curso no se califican por logros");
        }
        return prueba;
    }

    /** Sólo las pruebas de Year/First admiten Final Mark manual; Parcial/Final siempre son suma de logros. */
    private boolean admiteNotaManual(Evaluacion evaluacion) {
        return Evaluacion.PRUEBA.equals(evaluacion.getEvaTipo())
                && CalificacionReglas.pruebasPorLogros(evaluacion.getComision().getCurso().getCurNombre());
    }

    private Evaluacion crearEvaluacion(Comision comision, String tipo) {
        Evaluacion evaluacion = new Evaluacion();
        evaluacion.setComision(comision);
        evaluacion.setEvaTipo(tipo);
        evaluacion.setEvaNombre(Evaluacion.PARCIAL.equals(tipo) ? "Parcial" : "Final");
        soporte.copiarLogrosDelCurso(evaluacion);
        auditar(evaluacion, soporte.usuarioActual());
        return evaluacionRepository.save(evaluacion);
    }

    private Map<Long, BigDecimal> valoresValidados(GuardarFilaLogrosDTO fila, Map<Long, EvaluacionLogro> logros,
                                                   String nombreAlumno) {
        Map<Long, BigDecimal> valores = new HashMap<>();
        if (fila.getValores() == null) {
            return valores;
        }
        fila.getValores().forEach((logroId, valor) -> {
            if (valor == null) {
                return;
            }
            EvaluacionLogro logro = logros.get(logroId);
            if (logro == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Logro inexistente en la evaluación: " + logroId);
            }
            CalificacionReglas.validarAporte(nombreAlumno, logro.getEloNombre(), valor, logro.getEloPeso());
            valores.put(logroId, CalificacionReglas.redondear(valor));
        });
        return valores;
    }

    /** Actualiza en el lugar (no borra y re-inserta) para no chocar con el único (calificación, logro). */
    private void sincronizarValores(Calificacion calificacion, Map<Long, BigDecimal> valores,
                                    Map<Long, EvaluacionLogro> logros) {
        calificacion.getValores().removeIf(v -> !valores.containsKey(v.getEvaluacionLogro().getEloId()));
        Map<Long, CalificacionLogro> existentes = calificacion.getValores().stream()
                .collect(Collectors.toMap(v -> v.getEvaluacionLogro().getEloId(), v -> v));
        valores.forEach((logroId, valor) -> {
            CalificacionLogro valorLogro = existentes.get(logroId);
            if (valorLogro == null) {
                valorLogro = new CalificacionLogro();
                valorLogro.setCalificacion(calificacion);
                valorLogro.setEvaluacionLogro(logros.get(logroId));
                calificacion.getValores().add(valorLogro);
            }
            valorLogro.setCloValor(valor);
        });
    }

    private PlanillaLogrosDTO armarPlanilla(Comision comision, Evaluacion evaluacion) {
        PlanillaLogrosDTO dto = new PlanillaLogrosDTO();
        dto.setEvaluacionId(evaluacion.getEvaId());
        dto.setTipo(evaluacion.getEvaTipo());
        dto.setNombre(evaluacion.getEvaNombre());
        dto.setNotaManual(admiteNotaManual(evaluacion));
        dto.setComisionId(comision.getComId());
        dto.setComision(comision.getComNombre());
        dto.setCurso(comision.getCurso().getCurNombre());
        dto.setAnio(comision.getComAnio());
        dto.setLogros(evaluacion.getLogros().stream().map(LogroDTO::new).toList());

        Map<Long, Calificacion> calificaciones = calificacionRepository.findByEvaluacionEvaId(evaluacion.getEvaId()).stream()
                .collect(Collectors.toMap(c -> c.getAlumno().getAluId(), c -> c));

        dto.setAlumnos(soporte.alumnosDePlanilla(comision.getComId(), calificaciones.keySet()).stream()
                .map(alumno -> {
                    FilaLogrosDTO fila = new FilaLogrosDTO();
                    soporte.completarFila(fila, alumno);
                    Calificacion calificacion = calificaciones.get(alumno.alumno().getAluId());
                    fila.setAusente(calificacion != null && calificacion.getCalAusente());
                    fila.setNota(calificacion != null ? calificacion.getCalNota() : null);
                    Map<Long, BigDecimal> valores = new HashMap<>();
                    if (calificacion != null) {
                        calificacion.getValores().forEach(v -> valores.put(v.getEvaluacionLogro().getEloId(), v.getCloValor()));
                    }
                    fila.setValores(valores);
                    return fila;
                })
                .toList());
        return dto;
    }
}
