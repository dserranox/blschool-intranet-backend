package ar.com.blschool.dto;

import ar.com.blschool.entity.Evaluacion;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class PruebaDTO {

    private Long id;
    private String nombre;
    private LocalDate fecha;
    /** 1 o 2; si no viene se propone por la fecha */
    private Integer etapa;

    public PruebaDTO(Evaluacion prueba) {
        this.id = prueba.getEvaId();
        this.nombre = prueba.getEvaNombre();
        this.fecha = prueba.getEvaFecha();
        this.etapa = prueba.getEvaEtapa();
    }
}
