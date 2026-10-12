package ar.com.blschool.dto;

import lombok.Getter;
import lombok.Setter;

/** Datos comunes de una fila de planilla. editable=false: el alumno ya no está inscripto pero tiene notas. */
@Getter
@Setter
public abstract class FilaAlumnoDTO {

    private Long alumnoId;
    private String apellidos;
    private String nombres;
    private Boolean editable;
}
