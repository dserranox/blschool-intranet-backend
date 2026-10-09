package ar.com.blschool.controller;

import ar.com.blschool.dto.PreinscripcionDTO;
import ar.com.blschool.service.PreinscripcionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/preinscripciones")
public class PreinscripcionController {

    private final PreinscripcionService preinscripcionService;

    public PreinscripcionController(PreinscripcionService preinscripcionService) {
        this.preinscripcionService = preinscripcionService;
    }

    /** Lista las preinscripciones de un año (por defecto el año siguiente). estado: PENDIENTE | ASIGNADA | DESCARTADA | TODOS */
    @GetMapping
    public List<PreinscripcionDTO> listar(@RequestParam(required = false) Integer anio,
                                          @RequestParam(required = false) String estado) {
        return preinscripcionService.listar(anio != null ? anio : LocalDate.now().getYear() + 1, estado);
    }

    @GetMapping("/anios")
    public List<Integer> obtenerAnios() {
        return preinscripcionService.obtenerAnios();
    }

    @PutMapping("/{id}/asignar")
    @PreAuthorize("hasRole('ADMIN')")
    public PreinscripcionDTO asignar(@PathVariable Long id, @RequestParam Long comisionId) {
        return preinscripcionService.asignarComision(id, comisionId);
    }

    @PutMapping("/{id}/descartar")
    @PreAuthorize("hasRole('ADMIN')")
    public PreinscripcionDTO descartar(@PathVariable Long id) {
        return preinscripcionService.descartar(id);
    }
}
