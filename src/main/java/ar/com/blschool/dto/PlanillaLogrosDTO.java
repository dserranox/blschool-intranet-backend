package ar.com.blschool.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PlanillaLogrosDTO {

    private Long evaluacionId;
    private String tipo;
    /** "Parcial", "Final" o el nombre de la prueba */
    private String nombre;
    /** true si admite Final Mark cargado a mano (pruebas de Year/First) */
    private Boolean notaManual;
    private Long comisionId;
    private String comision;
    private String curso;
    private Integer anio;
    private List<LogroDTO> logros;
    private List<FilaLogrosDTO> alumnos;
}
