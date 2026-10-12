package ar.com.blschool.service;

import ar.com.blschool.dto.CalificacionesComisionDTO;
import ar.com.blschool.dto.ResultadoEvaluacionDTO;
import ar.com.blschool.dto.ResultadoLogroDTO;
import ar.com.blschool.dto.ResultadoPruebaDTO;
import ar.com.blschool.entity.Calificacion;
import ar.com.blschool.entity.CalificacionLogro;
import ar.com.blschool.entity.Comision;
import ar.com.blschool.entity.Evaluacion;
import ar.com.blschool.repository.CalificacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class HistorialCalificacionesService {

    private final CalificacionRepository calificacionRepository;

    public HistorialCalificacionesService(CalificacionRepository calificacionRepository) {
        this.calificacionRepository = calificacionRepository;
    }

    /** Calificaciones del alumno agrupadas por comisión, del año más reciente al más antiguo. */
    @Transactional(readOnly = true)
    public List<CalificacionesComisionDTO> historial(Long alumnoId) {
        Map<Long, CalificacionesComisionDTO> porComision = new LinkedHashMap<>();
        for (Calificacion calificacion : calificacionRepository.findHistorialByAlumnoId(alumnoId)) {
            Evaluacion evaluacion = calificacion.getEvaluacion();
            Comision comision = evaluacion.getComision();
            CalificacionesComisionDTO grupo = porComision.computeIfAbsent(comision.getComId(), id -> {
                CalificacionesComisionDTO nuevo = new CalificacionesComisionDTO();
                nuevo.setAnio(comision.getComAnio());
                nuevo.setComisionId(id);
                nuevo.setComision(comision.getComNombre());
                nuevo.setCurso(comision.getCurso().getCurNombre());
                nuevo.setPruebas(new ArrayList<>());
                return nuevo;
            });
            switch (evaluacion.getEvaTipo()) {
                case Evaluacion.PARCIAL -> grupo.setParcial(resultado(calificacion));
                case Evaluacion.FINAL -> grupo.setFinalAnual(resultado(calificacion));
                default -> grupo.getPruebas().add(new ResultadoPruebaDTO(
                        evaluacion.getEvaNombre(), evaluacion.getEvaFecha(), evaluacion.getEvaEtapa(),
                        calificacion.getCalNota(), calificacion.getCalAusente()));
            }
        }
        return new ArrayList<>(porComision.values());
    }

    private ResultadoEvaluacionDTO resultado(Calificacion calificacion) {
        Map<Long, BigDecimal> valores = calificacion.getValores().stream()
                .collect(Collectors.toMap(v -> v.getEvaluacionLogro().getEloId(), CalificacionLogro::getCloValor));
        List<ResultadoLogroDTO> logros = calificacion.getEvaluacion().getLogros().stream()
                .map(l -> new ResultadoLogroDTO(l.getEloNombre(), l.getEloPeso(), valores.get(l.getEloId())))
                .toList();
        return new ResultadoEvaluacionDTO(calificacion.getCalNota(), calificacion.getCalAusente(), logros);
    }
}
