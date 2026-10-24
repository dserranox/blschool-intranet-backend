package ar.com.blschool.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PlanillaPruebasDTO {

    private Long comisionId;
    private String comision;
    private String curso;
    private Integer anio;
    /** Year/First: cada prueba se carga en su propia planilla de logros */
    private Boolean porLogros;
    private List<PruebaDTO> pruebas;
    private List<FilaPruebasDTO> alumnos;
}
