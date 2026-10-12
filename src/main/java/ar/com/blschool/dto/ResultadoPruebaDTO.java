package ar.com.blschool.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class ResultadoPruebaDTO {

    private String nombre;
    private LocalDate fecha;
    private Integer etapa;
    private BigDecimal nota;
    private Boolean ausente;
}
