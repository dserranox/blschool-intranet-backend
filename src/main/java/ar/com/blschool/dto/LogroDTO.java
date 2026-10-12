package ar.com.blschool.dto;

import ar.com.blschool.entity.EvaluacionLogro;
import ar.com.blschool.entity.Logro;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class LogroDTO {

    private Long id;
    private String nombre;
    private BigDecimal peso;
    private Integer orden;

    public LogroDTO(Logro logro) {
        this.id = logro.getLogId();
        this.nombre = logro.getLogNombre();
        this.peso = logro.getLogPeso();
        this.orden = logro.getLogOrden();
    }

    public LogroDTO(EvaluacionLogro logro) {
        this.id = logro.getEloId();
        this.nombre = logro.getEloNombre();
        this.peso = logro.getEloPeso();
        this.orden = logro.getEloOrden();
    }
}
