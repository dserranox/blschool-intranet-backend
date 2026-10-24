package ar.com.blschool.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
public class GuardarFilaLogrosDTO {

    private Long alumnoId;
    private Boolean ausente;
    /** id de logro de la evaluación -> aporte (null = sin cargar) */
    private Map<Long, BigDecimal> valores;
    /** Final Mark cargado a mano (sólo pruebas de Year/First); excluyente con valores */
    private BigDecimal nota;
}
