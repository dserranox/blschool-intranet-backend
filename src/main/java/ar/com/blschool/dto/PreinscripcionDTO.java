package ar.com.blschool.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PreinscripcionDTO {

    private Long id;
    private Integer anio;
    private String estado;
    private String origen;
    private LocalDateTime fechaFormulario;
    private String escuelaCursoTurno;
    private Boolean alumnoNuevo;

    private Long alumnoId;
    private String apellidos;
    private String nombres;
    private String dni;
    private String alumnoEstado;

    /** Comisión asignada para el año de la preinscripción. */
    private Long comisionId;
    private String comision;
    private String curso;

    /** Comisión en la que el alumno cursa el año anterior al de la preinscripción (referencia para asignar). */
    private String comisionAnterior;
    private String cursoAnterior;
}
