package ar.com.blschool.service;

import ar.com.blschool.entity.Curso;
import ar.com.blschool.repository.ComisionRepository;
import ar.com.blschool.repository.CursoRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class CursoService extends BaseService {

    private final CursoRepository cursoRepository;
    private final ComisionRepository comisionRepository;

    public CursoService(CursoRepository cursoRepository, ComisionRepository comisionRepository) {
        this.cursoRepository = cursoRepository;
        this.comisionRepository = comisionRepository;
    }

    @Transactional
    public Curso crear(Curso curso) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        auditar(curso, username);
        return cursoRepository.save(curso);
    }

    @Transactional
    public Curso actualizar(Long id, Curso datos) {
        Curso existente = cursoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Curso no encontrado con id: " + id));
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        existente.setCurCodigo(datos.getCurCodigo());
        existente.setCurNombre(datos.getCurNombre());
        existente.setCurDescripcion(datos.getCurDescripcion());
        auditar(existente, username);
        return cursoRepository.save(existente);
    }

    @Transactional
    public void eliminar(Long id) {
        Curso existente = cursoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Curso no encontrado con id: " + id));
        if (comisionRepository.existsByCursoCurId(id)) {
            throw new RuntimeException("No se puede eliminar curso ya que se encuentra asociado a una comision");
        }
        cursoRepository.delete(existente);
    }

    @Transactional(readOnly = true)
    public List<Curso> buscar(String filtro) {
        if (StringUtils.hasText(filtro)) {
            return cursoRepository.findByFiltro(filtro.trim());
        }
        return cursoRepository.findAll();
    }
}
