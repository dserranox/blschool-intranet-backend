package ar.com.blschool.controller;

import ar.com.blschool.dto.*;
import ar.com.blschool.service.EvaluacionLogrosService;
import ar.com.blschool.service.PermisoCalificacionService;
import ar.com.blschool.service.PruebaService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Carga de calificaciones. La restricción "sólo sus comisiones" la aplican los servicios. */
@RestController
@RequestMapping("/calificaciones")
@PreAuthorize("hasAnyRole('ADMIN', 'DOCENTE')")
public class CalificacionController {

    private final PermisoCalificacionService permisos;
    private final EvaluacionLogrosService evaluacionLogrosService;
    private final PruebaService pruebaService;

    public CalificacionController(PermisoCalificacionService permisos,
                                  EvaluacionLogrosService evaluacionLogrosService,
                                  PruebaService pruebaService) {
        this.permisos = permisos;
        this.evaluacionLogrosService = evaluacionLogrosService;
        this.pruebaService = pruebaService;
    }

    /** Guardados concurrentes que chocan contra un índice único. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> conflicto() {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "status", 409,
                "error", "Conflict",
                "message", "Otro usuario estaba guardando esta planilla al mismo tiempo. Recargá y volvé a intentar."));
    }

    /** Comisiones del año que el usuario puede calificar (ADMIN: todas). */
    @GetMapping("/comisiones")
    @Transactional(readOnly = true)
    public List<ComisionDTO> comisiones(@RequestParam(required = false) Integer anio) {
        int anioConsulta = anio != null ? anio : LocalDate.now().getYear();
        return permisos.comisionesCalificables(anioConsulta).stream()
                .map(c -> {
                    ComisionDTO dto = new ComisionDTO();
                    dto.setComId(c.getComId());
                    dto.setAnio(c.getComAnio());
                    dto.setNombre(c.getComNombre());
                    dto.setActiva(c.getComActiva());
                    dto.setCursoId(c.getCurso().getCurId());
                    dto.setCursoNombre(c.getCurso().getCurNombre());
                    return dto;
                })
                .toList();
    }

    @GetMapping("/comision/{comisionId}/pruebas")
    public PlanillaPruebasDTO planillaPruebas(@PathVariable Long comisionId) {
        return pruebaService.obtenerPlanilla(comisionId);
    }

    @PostMapping("/comision/{comisionId}/pruebas")
    @ResponseStatus(HttpStatus.CREATED)
    public PruebaDTO crearPrueba(@PathVariable Long comisionId, @RequestBody PruebaDTO prueba) {
        return pruebaService.crear(comisionId, prueba);
    }

    @PutMapping("/comision/{comisionId}/pruebas")
    public PlanillaPruebasDTO guardarPruebas(@PathVariable Long comisionId, @RequestBody List<GuardarNotaPruebaDTO> notas) {
        return pruebaService.guardarNotas(comisionId, notas);
    }

    @PutMapping("/pruebas/{pruebaId}")
    public PruebaDTO actualizarPrueba(@PathVariable Long pruebaId, @RequestBody PruebaDTO prueba) {
        return pruebaService.actualizar(pruebaId, prueba);
    }

    @DeleteMapping("/pruebas/{pruebaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarPrueba(@PathVariable Long pruebaId) {
        pruebaService.eliminar(pruebaId);
    }

    /** Planilla por logros de una prueba (sólo cursos Year/First). */
    @GetMapping("/pruebas/{pruebaId}/logros")
    public PlanillaLogrosDTO planillaLogrosPrueba(@PathVariable Long pruebaId) {
        return evaluacionLogrosService.obtenerPlanillaPrueba(pruebaId);
    }

    @PutMapping("/pruebas/{pruebaId}/logros")
    public PlanillaLogrosDTO guardarLogrosPrueba(@PathVariable Long pruebaId, @RequestBody List<GuardarFilaLogrosDTO> filas) {
        return evaluacionLogrosService.guardarPlanillaPrueba(pruebaId, filas);
    }

    /** tipo: PARCIAL | FINAL ("pruebas" lo toma el mapeo literal de arriba). */
    @GetMapping("/comision/{comisionId}/{tipo}")
    public PlanillaLogrosDTO planillaLogros(@PathVariable Long comisionId, @PathVariable String tipo) {
        return evaluacionLogrosService.obtenerPlanilla(comisionId, tipo);
    }

    @PutMapping("/comision/{comisionId}/{tipo}")
    public PlanillaLogrosDTO guardarLogros(@PathVariable Long comisionId, @PathVariable String tipo,
                                           @RequestBody List<GuardarFilaLogrosDTO> filas) {
        return evaluacionLogrosService.guardarPlanilla(comisionId, tipo, filas);
    }
}
