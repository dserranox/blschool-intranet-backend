package ar.com.blschool.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PerfilDTO {
    private Long personaId;
    private String nombres;
    private String apellidos;
    private String email;
    private String username;
    private List<String> roles;
}
