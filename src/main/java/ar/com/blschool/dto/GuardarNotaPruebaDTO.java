package ar.com.blschool.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class GuardarNotaPruebaDTO {

    private Long alumnoId;
    private Long evaluacionId;
    /** null y ausente=false -> se borra la nota */
    private BigDecimal nota;
    private Boolean ausente;
}
