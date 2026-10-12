package ar.com.blschool.service;

import ar.com.blschool.dto.LogroDTO;
import ar.com.blschool.entity.Curso;
import ar.com.blschool.entity.Logro;
import ar.com.blschool.repository.CursoRepository;
import ar.com.blschool.repository.LogroRepository;
import ar.com.blschool.util.CalificacionReglas;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuración de logros por curso. Cambiarla NO afecta Parcial/Final ya creados
 * (tienen su propia copia en EVALUACION_LOGROS).
 */
@Service
public class LogroService extends BaseService {

    private final LogroRepository logroRepository;
    private final CursoRepository cursoRepository;

    public LogroService(LogroRepository logroRepository, CursoRepository cursoRepository) {
        this.logroRepository = logroRepository;
        this.cursoRepository = cursoRepository;
    }

    @Transactional(readOnly = true)
    public List<LogroDTO> listar(Long cursoId) {
        return logroRepository.findByCursoCurIdOrderByLogOrdenAsc(cursoId).stream()
                .map(LogroDTO::new)
                .toList();
    }

    /** Reemplaza la configuración completa. El orden es el de la lista recibida. */
    @Transactional
    public List<LogroDTO> reemplazar(Long cursoId, List<LogroDTO> logros) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Curso no encontrado con id: " + cursoId));

        if (logros.contains(null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Logro vacío en la lista");
        }
        CalificacionReglas.validarLogros(
                logros.stream().map(LogroDTO::getNombre).toList(),
                logros.stream().map(LogroDTO::getPeso).toList());

        logroRepository.deleteAll(logroRepository.findByCursoCurIdOrderByLogOrdenAsc(cursoId));
        logroRepository.flush();

        List<Logro> nuevos = new ArrayList<>();
        for (int i = 0; i < logros.size(); i++) {
            Logro logro = new Logro();
            logro.setCurso(curso);
            logro.setLogNombre(logros.get(i).getNombre().trim());
            logro.setLogPeso(CalificacionReglas.redondear(logros.get(i).getPeso()));
            logro.setLogOrden(i + 1);
            auditar(logro, username);
            nuevos.add(logro);
        }
        logroRepository.saveAll(nuevos);
        return nuevos.stream().map(LogroDTO::new).toList();
    }
}
