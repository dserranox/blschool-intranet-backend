package ar.com.blschool.controller;

import ar.com.blschool.dto.DocenteDTO;
import ar.com.blschool.service.DocenteService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/docentes")
public class DocenteController {

    private final DocenteService docenteService;

    public DocenteController(DocenteService docenteService) {
        this.docenteService = docenteService;
    }

    @GetMapping
    public List<DocenteDTO> listar() {
        return docenteService.listar();
    }

    @GetMapping("/{id}")
    public DocenteDTO obtenerPorId(@PathVariable Long id) {
        return docenteService.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public DocenteDTO crear(@RequestBody DocenteDTO dto) {
        return docenteService.crear(dto);
    }

    @PutMapping("/{id}")
    public DocenteDTO modificar(@PathVariable Long id, @RequestBody DocenteDTO dto) {
        return docenteService.modificar(id, dto);
    }

    @PatchMapping("/{id}/activar")
    @PreAuthorize("hasRole('ADMIN')")
    public void activar(@PathVariable Long id) {
        docenteService.cambiarEstado(id, true);
    }

    @PatchMapping("/{id}/desactivar")
    @PreAuthorize("hasRole('ADMIN')")
    public void desactivar(@PathVariable Long id) {
        docenteService.cambiarEstado(id, false);
    }
}
