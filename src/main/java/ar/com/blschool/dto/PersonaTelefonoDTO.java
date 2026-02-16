package ar.com.blschool.dto;

import ar.com.blschool.entity.PersonaTelefono;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PersonaTelefonoDTO {

    private Long id;
    private String numero;
    private String tipo;
    private String nota;
    private Boolean principal;

    public PersonaTelefonoDTO(PersonaTelefono telefono) {
        this.id = telefono.getPteId();
        this.numero = telefono.getPteNumero();
        this.tipo = telefono.getPteTipo();
        this.nota = telefono.getPteNota();
        this.principal = telefono.getPtePrincipal();
    }
}
