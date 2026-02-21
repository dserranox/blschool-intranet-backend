package ar.com.blschool.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AlumnoDTO {

    private Long id;
    private String apellidos;
    private String nombres;
    private String dni;
    private LocalDate fechaNacimiento;
    private String email;
    private String emailAlternativo;
    private String direccion;
    private String escuela;
    private String gradoCurso;
    private String estado;
    private Long comisionId;
    private String comision;
    private String curso;
    private List<PersonaTelefonoDTO> telefonos;
}
