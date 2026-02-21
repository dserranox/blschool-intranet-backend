package ar.com.blschool.controller;

import ar.com.blschool.dto.AlumnoDTO;
import ar.com.blschool.service.AlumnoService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @GetMapping("/{id}")
    public AlumnoDTO obtenerPorId(@PathVariable Long id) {
        return alumnoService.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public AlumnoDTO crear(@RequestBody AlumnoDTO dto) {
        return alumnoService.crear(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public AlumnoDTO modificar(@PathVariable Long id, @RequestBody AlumnoDTO dto) {
        return alumnoService.modificar(id, dto);
    }

    @PutMapping("/{id}/baja")
    @PreAuthorize("hasRole('ADMIN')")
    public void darDeBaja(@PathVariable Long id) {
        alumnoService.darDeBaja(id);
    }
}
