package ar.com.blschool.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
public class FilaLogrosDTO extends FilaAlumnoDTO {

    private Boolean ausente;
    private BigDecimal nota;
    /** id de logro de la evaluación -> aporte */
    private Map<Long, BigDecimal> valores;
}
