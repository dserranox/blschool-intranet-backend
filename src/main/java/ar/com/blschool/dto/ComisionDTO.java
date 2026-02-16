package ar.com.blschool.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ComisionDTO {

    private Long comId;
    private Integer anio;
    private String nombre;
    private Integer cupo;
    private Boolean activa;
    private Long inscriptos;
    private Long preInscriptos;

    @JsonProperty("curso_id")
    private Long cursoId;
    private String cursoNombre;
    private List<ComisionClaseDTO> clases;
}
