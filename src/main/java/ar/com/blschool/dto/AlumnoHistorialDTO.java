package ar.com.blschool.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AlumnoHistorialDTO {

    private List<InscripcionHistorialDTO> inscripciones;
    private List<PreinscripcionDTO> preinscripciones;
}
