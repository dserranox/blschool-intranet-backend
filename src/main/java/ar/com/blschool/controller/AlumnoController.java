package ar.com.blschool.controller;

import ar.com.blschool.dto.AlumnoDTO;
import ar.com.blschool.dto.AlumnoHistorialDTO;
import ar.com.blschool.dto.CalificacionesComisionDTO;
import ar.com.blschool.service.AlumnoService;
import ar.com.blschool.service.HistorialCalificacionesService;
import ar.com.blschool.service.PreinscripcionService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/alumnos")
public class AlumnoController {

    private final AlumnoService alumnoService;
    private final PreinscripcionService preinscripcionService;

    private final HistorialCalificacionesService historialCalificacionesService;

    public AlumnoController(AlumnoService alumnoService, PreinscripcionService preinscripcionService,
                            HistorialCalificacionesService historialCalificacionesService) {
        this.alumnoService = alumnoService;
        this.preinscripcionService = preinscripcionService;
        this.historialCalificacionesService = historialCalificacionesService;
    }

    /** Calificaciones del alumno en todos los años (Parcial/Final por logro y pruebas). */
    @GetMapping("/{id}/calificaciones")
    public List<CalificacionesComisionDTO> calificaciones(@PathVariable Long id) {
        return historialCalificacionesService.historial(id);
    }

    /** Todas las inscripciones (todos los años y estados) y preinscripciones del alumno. */
    @GetMapping("/{id}/historial")
    public AlumnoHistorialDTO historial(@PathVariable Long id) {
        return new AlumnoHistorialDTO(
                alumnoService.historialInscripciones(id),
                preinscripcionService.listarPorAlumno(id));
    }

    @GetMapping
    public List<AlumnoDTO> listar(@RequestParam(required = false) String estado) {
        return alumnoService.listar(estado);
    }

    @GetMapping("/{id}")
    public AlumnoDTO obtenerPorId(@PathVariable Long id) {
        return alumnoService.obtenerPorId(id);
    }

    @GetMapping("/comision/{comisionId}")
    public List<AlumnoDTO> listarPorComision(@PathVariable Long comisionId) {
        return alumnoService.listarPorComision(comisionId);
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
