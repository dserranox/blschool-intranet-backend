package ar.com.blschool.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class FilaPruebasDTO extends FilaAlumnoDTO {

    /** id de prueba -> nota (sólo las cargadas) */
    private Map<Long, NotaPruebaDTO> notas;
}
