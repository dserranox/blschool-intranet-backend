package ar.com.blschool.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class InscripcionHistorialDTO {

    private Long id;
    private Integer anio;
    private Long comisionId;
    private String comision;
    private String curso;
    private String estado;
    private LocalDateTime fecha;
}
