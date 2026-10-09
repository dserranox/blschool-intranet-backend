package ar.com.blschool.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Respuesta del Google Form tal como la envía el Apps Script.
 * Todo llega como texto: la normalización la hace el backend.
 */
@Getter
@Setter
public class PreinscripcionFormDTO {

    /** Año al que se preinscribe. Si no viene se usa el año de la marca temporal + 1. */
    private Integer anio;
    private String marcaTemporal;
    private String nombre;
    private String apellido;
    private String dni;
    private String fechaNacimiento;
    private String telefonoMadre;
    private String telefonoPadre;
    private String telefonoAlumno;
    private String domicilio;
    private String email;
    /** Email recolectado automáticamente por Google Forms (se usa si "email" viene vacío). */
    private String emailFormulario;
    private String escuelaCursoTurno;
    private String tieneProblemaSalud;
    private String problemaSalud;
}
