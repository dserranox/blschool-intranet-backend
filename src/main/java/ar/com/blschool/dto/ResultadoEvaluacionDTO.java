package ar.com.blschool.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ResultadoEvaluacionDTO {

    private BigDecimal nota;
    private Boolean ausente;
    private List<ResultadoLogroDTO> logros;
}
