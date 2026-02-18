package ar.com.blschool.controller;

import ar.com.blschool.dto.AlumnoDTO;
import ar.com.blschool.service.AlumnoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/alumnos")
public class AlumnoController {

    private final AlumnoService alumnoService;

    public AlumnoController(AlumnoService alumnoService) {
        this.alumnoService = alumnoService;
    }

    @GetMapping
    public List<AlumnoDTO> listar(@RequestParam(required = false) String estado) {
        return alumnoService.listar(estado);
    }
}
