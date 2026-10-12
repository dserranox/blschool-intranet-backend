package ar.com.blschool.service;

import ar.com.blschool.dto.FilaLogrosDTO;
import ar.com.blschool.dto.GuardarFilaLogrosDTO;
import ar.com.blschool.dto.LogroDTO;
import ar.com.blschool.dto.PlanillaLogrosDTO;
import ar.com.blschool.entity.*;
import ar.com.blschool.repository.CalificacionRepository;
import ar.com.blschool.repository.EvaluacionRepository;
import ar.com.blschool.repository.LogroRepository;
import ar.com.blschool.util.CalificacionReglas;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/** Planilla de Parcial / Final de una comisión (calificación por logros). */
@Service
public class EvaluacionLogrosService extends BaseService {

    private final EvaluacionRepository evaluacionRepository;
    private final CalificacionRepository calificacionRepository;
    private final LogroRepository logroRepository;
    private final PermisoCalificacionService permisos;
    private final CalificacionSoporte soporte;

    public EvaluacionLogrosService(EvaluacionRepository evaluacionRepository,
                                   CalificacionRepository calificacionRepository,
                                   LogroRepository logroRepository,
                                   PermisoCalificacionService permisos,
                                   CalificacionSoporte soporte) {
        this.evaluacionRepository = evaluacionRepository;
        this.calificacionRepository = calificacionRepository;
        this.logroRepository = logroRepository;
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
        Map<Long, EvaluacionLogro> logros = evaluacion.getLogros().stream()
                .collect(Collectors.toMap(EvaluacionLogro::getEloId, l -> l));
        Map<Long, Alumno> inscriptos = soporte.alumnosInscriptos(comisionId);
        String username = soporte.usuarioActual();

        for (GuardarFilaLogrosDTO fila : filas) {
            if (fila == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fila vacía en la planilla");
            }
            Alumno alumno = soporte.alumnoEditable(inscriptos, fila.getAlumnoId());
            boolean ausente = Boolean.TRUE.equals(fila.getAusente());
            Map<Long, BigDecimal> valores = ausente
                    ? new HashMap<>()
                    : valoresValidados(fila, logros, soporte.nombre(alumno));
            Calificacion calificacion = calificacionRepository
                    .findByEvaluacionEvaIdAndAlumnoAluId(evaluacion.getEvaId(), alumno.getAluId())
                    .orElse(null);

            if (!ausente && valores.isEmpty()) {
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
            } else {
                sincronizarValores(calificacion, valores, logros);
                calificacion.setCalNota(CalificacionReglas.calcularNota(logros.keySet(), valores));
            }
            auditar(calificacion, username);
            calificacionRepository.save(calificacion);
        }
        return armarPlanilla(comision, evaluacion);
    }

    private String validarTipo(String tipo) {
        String valor = tipo == null ? "" : tipo.toUpperCase();
        if (!Evaluacion.PARCIAL.equals(valor) && !Evaluacion.FINAL.equals(valor)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de evaluación inválido: " + tipo);
        }
        return valor;
    }

    private Evaluacion crearEvaluacion(Comision comision, String tipo) {
        List<Logro> logrosCurso = logroRepository.findByCursoCurIdOrderByLogOrdenAsc(comision.getCurso().getCurId());
        if (logrosCurso.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Configurá los logros de " + comision.getCurso().getCurNombre() + " primero");
        }
        Evaluacion evaluacion = new Evaluacion();
        evaluacion.setComision(comision);
        evaluacion.setEvaTipo(tipo);
        evaluacion.setEvaNombre(Evaluacion.PARCIAL.equals(tipo) ? "Parcial" : "Final");
        for (Logro logro : logrosCurso) {
            EvaluacionLogro copia = new EvaluacionLogro();
            copia.setEvaluacion(evaluacion);
            copia.setEloNombre(logro.getLogNombre());
            copia.setEloPeso(logro.getLogPeso());
            copia.setEloOrden(logro.getLogOrden());
            evaluacion.getLogros().add(copia);
        }
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
