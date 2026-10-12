package ar.com.blschool.controller;

import ar.com.blschool.dto.LogroDTO;
import ar.com.blschool.service.LogroService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cursos/{cursoId}/logros")
public class LogroController {

    private final LogroService logroService;

    public LogroController(LogroService logroService) {
        this.logroService = logroService;
    }

    @GetMapping
    public List<LogroDTO> listar(@PathVariable Long cursoId) {
        return logroService.listar(cursoId);
    }

    /** Reemplaza todos los logros del curso (deben sumar 100; lista vacía = sin configuración). */
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<LogroDTO> reemplazar(@PathVariable Long cursoId, @RequestBody List<LogroDTO> logros) {
        return logroService.reemplazar(cursoId, logros);
    }
}
