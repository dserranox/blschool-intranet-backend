package ar.com.blschool.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PreinscripcionResultadoDTO {

    private Long preinscripcionId;
    private Long alumnoId;
    private Integer anio;
    /** true si el alumno no existía y se creó con esta preinscripción. */
    private Boolean alumnoNuevo;
    /** true si la preinscripción se creó; false si ya existía para ese año y se actualizó. */
    private Boolean preinscripcionNueva;
}
