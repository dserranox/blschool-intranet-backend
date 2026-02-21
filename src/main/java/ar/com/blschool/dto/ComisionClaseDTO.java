package ar.com.blschool.dto;

import ar.com.blschool.entity.ComisionClase;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ComisionClaseDTO {

    private Long id;

    private Integer dia;

    @JsonProperty("hora_desde")
    private LocalTime horaDesde;

    @JsonProperty("hora_hasta")
    private LocalTime horaHasta;

    private Long docente;

    @JsonProperty("docente_suplente")
    private Long docenteSuplente;

    @JsonProperty("docente_nombre")
    private String docenteNombre;

    @JsonProperty("docente_suplente_nombre")
    private String docenteSuplenteNombre;

    public ComisionClaseDTO(ComisionClase comisionClase) {
        this.id = comisionClase.getClcId();
        this.dia = comisionClase.getClcDiaSemana();
        this.horaDesde = comisionClase.getClcHoraDesde();
        this.horaHasta = comisionClase.getClcHoraHasta();
        if (comisionClase.getDocente() != null) {
            this.docente = comisionClase.getDocente().getPerId();
            this.docenteNombre = comisionClase.getDocente().getPerApellido() + ", " + comisionClase.getDocente().getPerNombres();
        }
        if (comisionClase.getDocenteSuplente() != null) {
            this.docenteSuplente = comisionClase.getDocenteSuplente().getPerId();
            this.docenteSuplenteNombre = comisionClase.getDocenteSuplente().getPerApellido() + ", " + comisionClase.getDocenteSuplente().getPerNombres();
        }
    }
}
