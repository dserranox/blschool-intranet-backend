package ar.com.blschool.service;

import ar.com.blschool.dto.*;
import ar.com.blschool.entity.Alumno;
import ar.com.blschool.entity.Calificacion;
import ar.com.blschool.entity.Comision;
import ar.com.blschool.entity.Evaluacion;
import ar.com.blschool.repository.CalificacionRepository;
import ar.com.blschool.repository.EvaluacionRepository;
import ar.com.blschool.util.CalificacionReglas;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

/** Pruebas de una comisión: las crea la docente; nota única 0-100 (Year/First: por logros, ver EvaluacionLogrosService); no ponderan. */
@Service
public class PruebaService extends BaseService {

    private final EvaluacionRepository evaluacionRepository;
    private final CalificacionRepository calificacionRepository;
    private final PermisoCalificacionService permisos;
    private final CalificacionSoporte soporte;

    public PruebaService(EvaluacionRepository evaluacionRepository,
                         CalificacionRepository calificacionRepository,
                         PermisoCalificacionService permisos,
                         CalificacionSoporte soporte) {
        this.evaluacionRepository = evaluacionRepository;
        this.calificacionRepository = calificacionRepository;
        this.permisos = permisos;
        this.soporte = soporte;
    }

    @Transactional(readOnly = true)
    public PlanillaPruebasDTO obtenerPlanilla(Long comisionId) {
        permisos.verificarComision(comisionId);
        return armarPlanilla(soporte.buscarComision(comisionId));
    }

    @Transactional
    public PruebaDTO crear(Long comisionId, PruebaDTO datos) {
        permisos.verificarComision(comisionId);
        Evaluacion prueba = new Evaluacion();
        prueba.setComision(soporte.buscarComision(comisionId));
        prueba.setEvaTipo(Evaluacion.PRUEBA);
        if (porLogros(prueba.getComision())) {
            soporte.copiarLogrosDelCurso(prueba);
        }
        aplicarDatos(prueba, datos);
        auditar(prueba, soporte.usuarioActual());
        return new PruebaDTO(evaluacionRepository.save(prueba));
    }

    @Transactional
    public PruebaDTO actualizar(Long pruebaId, PruebaDTO datos) {
        Evaluacion prueba = buscarPrueba(pruebaId);
        aplicarDatos(prueba, datos);
        auditar(prueba, soporte.usuarioActual());
        return new PruebaDTO(evaluacionRepository.save(prueba));
    }

    /** Elimina la prueba; sus notas se borran por ON DELETE CASCADE. */
    @Transactional
    public void eliminar(Long pruebaId) {
        evaluacionRepository.delete(buscarPrueba(pruebaId));
    }

    /** Guarda las notas recibidas (sólo las modificadas). Todo-o-nada. */
    @Transactional
    public PlanillaPruebasDTO guardarNotas(Long comisionId, List<GuardarNotaPruebaDTO> notas) {
        permisos.verificarComision(comisionId);
        Comision comision = soporte.buscarComision(comisionId);
        if (porLogros(comision)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "En " + comision.getCurso().getCurNombre() + " cada prueba se carga desde su planilla de logros");
        }
        Map<Long, Evaluacion> pruebas = evaluacionRepository
                .findByComisionComIdAndEvaTipoOrderByEvaFechaAscEvaIdAsc(comisionId, Evaluacion.PRUEBA).stream()
                .collect(Collectors.toMap(Evaluacion::getEvaId, p -> p));
        Map<Long, Alumno> inscriptos = soporte.alumnosInscriptos(comisionId);
        String username = soporte.usuarioActual();

        for (GuardarNotaPruebaDTO dato : notas) {
            if (dato == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nota vacía en la planilla");
            }
            Alumno alumno = soporte.alumnoEditable(inscriptos, dato.getAlumnoId());
            Evaluacion prueba = pruebas.get(dato.getEvaluacionId());
            if (prueba == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "La prueba " + dato.getEvaluacionId() + " no pertenece a la comisión");
            }
            boolean ausente = Boolean.TRUE.equals(dato.getAusente());
            if (!ausente) {
                CalificacionReglas.validarNotaPrueba(soporte.nombre(alumno), prueba.getEvaNombre(), dato.getNota());
            }
            Calificacion calificacion = calificacionRepository
                    .findByEvaluacionEvaIdAndAlumnoAluId(prueba.getEvaId(), alumno.getAluId())
                    .orElse(null);

            if (!ausente && dato.getNota() == null) {
                if (calificacion != null) {
                    calificacionRepository.delete(calificacion);
                }
                continue;
            }
            if (calificacion == null) {
                calificacion = soporte.nuevaCalificacion(prueba, alumno);
            }
            calificacion.setCalAusente(ausente);
            calificacion.setCalNota(ausente ? null : CalificacionReglas.redondear(dato.getNota()));
            auditar(calificacion, username);
            calificacionRepository.save(calificacion);
        }
        return armarPlanilla(comision);
    }

    private boolean porLogros(Comision comision) {
        return CalificacionReglas.pruebasPorLogros(comision.getCurso().getCurNombre());
    }

    private Evaluacion buscarPrueba(Long pruebaId) {
        Evaluacion prueba = evaluacionRepository.findById(pruebaId)
                .filter(e -> Evaluacion.PRUEBA.equals(e.getEvaTipo()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prueba no encontrada con id: " + pruebaId));
        permisos.verificarComision(prueba.getComision().getComId());
        return prueba;
    }

    private void aplicarDatos(Evaluacion prueba, PruebaDTO datos) {
        String nombre = datos.getNombre() == null ? "" : datos.getNombre().trim();
        if (nombre.isEmpty() || nombre.length() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La prueba necesita un nombre (hasta 100 caracteres)");
        }
        if (datos.getFecha() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La prueba necesita una fecha");
        }
        int etapa = datos.getEtapa() != null ? datos.getEtapa() : CalificacionReglas.etapaPorFecha(datos.getFecha());
        if (etapa != 1 && etapa != 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La etapa debe ser 1 o 2");
        }
        prueba.setEvaNombre(nombre);
        prueba.setEvaFecha(datos.getFecha());
        prueba.setEvaEtapa(etapa);
    }

    private PlanillaPruebasDTO armarPlanilla(Comision comision) {
        List<Evaluacion> pruebas = evaluacionRepository
                .findByComisionComIdAndEvaTipoOrderByEvaFechaAscEvaIdAsc(comision.getComId(), Evaluacion.PRUEBA);
        List<Long> ids = pruebas.stream().map(Evaluacion::getEvaId).toList();
        List<Calificacion> calificaciones = ids.isEmpty() ? List.of() : calificacionRepository.findByEvaluacionEvaIdIn(ids);

        Map<Long, Map<Long, NotaPruebaDTO>> notasPorAlumno = new HashMap<>();
        for (Calificacion c : calificaciones) {
            notasPorAlumno.computeIfAbsent(c.getAlumno().getAluId(), k -> new HashMap<>())
                    .put(c.getEvaluacion().getEvaId(), new NotaPruebaDTO(c.getCalNota(), c.getCalAusente()));
        }

        PlanillaPruebasDTO dto = new PlanillaPruebasDTO();
        dto.setComisionId(comision.getComId());
        dto.setComision(comision.getComNombre());
        dto.setCurso(comision.getCurso().getCurNombre());
        dto.setAnio(comision.getComAnio());
        dto.setPorLogros(porLogros(comision));
        dto.setPruebas(pruebas.stream().map(PruebaDTO::new).toList());
        dto.setAlumnos(soporte.alumnosDePlanilla(comision.getComId(), notasPorAlumno.keySet()).stream()
                .map(alumno -> {
                    FilaPruebasDTO fila = new FilaPruebasDTO();
                    soporte.completarFila(fila, alumno);
                    fila.setNotas(notasPorAlumno.getOrDefault(alumno.alumno().getAluId(), Map.of()));
                    return fila;
                })
                .toList());
        return dto;
    }
}
