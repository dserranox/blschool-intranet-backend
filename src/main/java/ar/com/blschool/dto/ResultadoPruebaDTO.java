package ar.com.blschool.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ResultadoPruebaDTO {

    private String nombre;
    private LocalDate fecha;
    private Integer etapa;
    private BigDecimal nota;
    private Boolean ausente;
    /** Aportes por logro (vacío si la nota es única o Final Mark manual) */
    private List<ResultadoLogroDTO> logros;
}
