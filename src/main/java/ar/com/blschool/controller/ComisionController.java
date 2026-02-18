package ar.com.blschool.controller;

import ar.com.blschool.dto.ComisionDTO;
import ar.com.blschool.entity.Comision;
import ar.com.blschool.service.ComisionService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comisiones")
public class ComisionController {

    private final ComisionService comisionService;

    public ComisionController(ComisionService comisionService) {
        this.comisionService = comisionService;
    }

    @GetMapping("/anios")
    public List<Integer> obtenerAniosDisponibles() {
        return comisionService.obtenerAniosDisponibles();
    }

    @GetMapping("/anio/{anio}")
    public List<ComisionDTO> buscarPorAnio(@PathVariable Integer anio) {
        return comisionService.buscarPorAnio(anio);
    }

    @GetMapping("/curso/{cursoId}")
    public List<ComisionDTO> buscarPorCurso(@PathVariable Long cursoId) {
        return comisionService.buscarPorCurso(cursoId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public Comision crear(@RequestBody ComisionDTO dto) {
        return comisionService.crear(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ComisionDTO modificar(@PathVariable Long id, @RequestBody ComisionDTO dto) {
        return comisionService.modificar(id, dto);
    }

    @PatchMapping("/{id}/desactivar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void desactivar(@PathVariable Long id) {
        comisionService.desactivar(id);
    }
}
