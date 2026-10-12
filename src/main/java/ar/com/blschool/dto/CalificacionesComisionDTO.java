package ar.com.blschool.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/** Calificaciones de un alumno en una comisión (un año). */
@Getter
@Setter
public class CalificacionesComisionDTO {

    private Integer anio;
    private Long comisionId;
    private String comision;
    private String curso;
    private ResultadoEvaluacionDTO parcial;
    @JsonProperty("final")
    private ResultadoEvaluacionDTO finalAnual;
    private List<ResultadoPruebaDTO> pruebas;
}
