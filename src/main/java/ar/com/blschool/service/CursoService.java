package ar.com.blschool.service;

import ar.com.blschool.dto.CursoDTO;
import ar.com.blschool.entity.Comision;
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
    public CursoDTO obtenerPorId(Long id) {
        Curso curso = cursoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Curso no encontrado con id: " + id));
        return toDTO(curso);
    }

    @Transactional(readOnly = true)
    public List<CursoDTO> buscar(String filtro) {
        List<Curso> cursos;
        if (StringUtils.hasText(filtro)) {
            cursos = cursoRepository.findByFiltro(filtro.trim());
        } else {
            cursos = cursoRepository.findAll();
        }
        return cursos.stream().map(this::toDTO).toList();
    }

    private CursoDTO toDTO(Curso curso) {
        CursoDTO dto = new CursoDTO();
        dto.setCurId(curso.getCurId());
        dto.setCurCodigo(curso.getCurCodigo());
        dto.setCurNombre(curso.getCurNombre());
        dto.setCurDescripcion(curso.getCurDescripcion());
        dto.setComisionesActivas(
                comisionRepository.findByCursoCurIdAndComActivaTrue(curso.getCurId())
                        .stream()
                        .map(Comision::getComNombre)
                        .toList()
        );
        return dto;
    }
}
