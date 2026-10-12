package ar.com.blschool.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class ResultadoLogroDTO {

    private String nombre;
    private BigDecimal peso;
    /** aporte cargado; null si no se cargó */
    private BigDecimal valor;
}
