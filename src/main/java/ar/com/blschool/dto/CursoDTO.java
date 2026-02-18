package ar.com.blschool.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CursoDTO {

    private Long curId;
    private String curCodigo;
    private String curNombre;
    private String curDescripcion;
    private List<String> comisionesActivas;
}
