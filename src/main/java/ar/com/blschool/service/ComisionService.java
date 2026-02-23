package ar.com.blschool.service;

import ar.com.blschool.dto.ComisionClaseDTO;
import ar.com.blschool.dto.ComisionDTO;
import ar.com.blschool.entity.Comision;
import ar.com.blschool.entity.ComisionClase;
import ar.com.blschool.entity.Curso;
import ar.com.blschool.entity.Persona;
import ar.com.blschool.repository.ComisionRepository;
import ar.com.blschool.repository.CursoRepository;
import ar.com.blschool.repository.PersonaRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ComisionService extends BaseService {

    private final ComisionRepository comisionRepository;
    private final CursoRepository cursoRepository;
    private final PersonaRepository personaRepository;

    public ComisionService(ComisionRepository comisionRepository,
                           CursoRepository cursoRepository,
                           PersonaRepository personaRepository) {
        this.comisionRepository = comisionRepository;
        this.cursoRepository = cursoRepository;
        this.personaRepository = personaRepository;
    }

    @Transactional
    public Comision crear(ComisionDTO dto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Curso curso = cursoRepository.findById(dto.getCursoId())
                .orElseThrow(() -> new RuntimeException("Curso no encontrado con id: " + dto.getCursoId()));

        Comision comision = new Comision();
        comision.setCurso(curso);
        comision.setComAnio(dto.getAnio());
        comision.setComNombre(dto.getNombre());
        comision.setComCupo(dto.getCupo());
        comision.setComActiva(true);
        auditar(comision, username);

        List<ComisionClase> clases = new ArrayList<>();
        if (dto.getClases() != null) {
            for (ComisionClaseDTO claseDTO : dto.getClases()) {
                ComisionClase clase = new ComisionClase();
                clase.setComision(comision);
                clase.setClcDiaSemana(claseDTO.getDia());
                clase.setClcHoraDesde(claseDTO.getHoraDesde());
                clase.setClcHoraHasta(claseDTO.getHoraHasta());

                if (claseDTO.getDocente() != null) {
                    Persona docente = personaRepository.findById(claseDTO.getDocente())
                            .orElseThrow(() -> new RuntimeException("Docente no encontrado con id: " + claseDTO.getDocente()));
                    clase.setDocente(docente);
                }

                if (claseDTO.getDocenteSuplente() != null) {
                    Persona suplente = personaRepository.findById(claseDTO.getDocenteSuplente())
                            .orElseThrow(() -> new RuntimeException("Docente suplente no encontrado con id: " + claseDTO.getDocenteSuplente()));
                    clase.setDocenteSuplente(suplente);
                }

                auditar(clase, username);
                clases.add(clase);
            }
        }
        comision.setComisionesClases(clases);

        return comisionRepository.save(comision);
    }

    @Transactional
    public ComisionDTO modificar(Long id, ComisionDTO dto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Comision comision = comisionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comision no encontrada con id: " + id));

        if (dto.getCursoId() != null) {
            Curso curso = cursoRepository.findById(dto.getCursoId())
                    .orElseThrow(() -> new RuntimeException("Curso no encontrado con id: " + dto.getCursoId()));
            comision.setCurso(curso);
        }
        comision.setComAnio(dto.getAnio());
        comision.setComNombre(dto.getNombre());
        comision.setComCupo(dto.getCupo());
        if (dto.getActiva() != null) {
            comision.setComActiva(dto.getActiva());
        }
        auditar(comision, username);

        // Sincronizar clases
        List<ComisionClase> clasesExistentes = comision.getComisionesClases();
        if (clasesExistentes == null) {
            clasesExistentes = new ArrayList<>();
            comision.setComisionesClases(clasesExistentes);
        }

        if (dto.getClases() != null) {
            // Recolectar IDs que vienen en el DTO
            Set<Long> idsEnDTO = dto.getClases().stream()
                    .map(ComisionClaseDTO::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            // Eliminar clases que ya no están en el DTO
            clasesExistentes.removeIf(clase -> !idsEnDTO.contains(clase.getClcId()));

            // Actualizar existentes y agregar nuevas
            for (ComisionClaseDTO claseDTO : dto.getClases()) {
                ComisionClase clase;
                if (claseDTO.getId() != null) {
                    clase = clasesExistentes.stream()
                            .filter(c -> c.getClcId().equals(claseDTO.getId()))
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("Clase no encontrada con id: " + claseDTO.getId()));
                } else {
                    clase = new ComisionClase();
                    clase.setComision(comision);
                    clasesExistentes.add(clase);
                }

                clase.setClcDiaSemana(claseDTO.getDia());
                clase.setClcHoraDesde(claseDTO.getHoraDesde());
                clase.setClcHoraHasta(claseDTO.getHoraHasta());

                if (claseDTO.getDocente() != null) {
                    Persona docente = personaRepository.findById(claseDTO.getDocente())
                            .orElseThrow(() -> new RuntimeException("Docente no encontrado con id: " + claseDTO.getDocente()));
                    clase.setDocente(docente);
                } else {
                    clase.setDocente(null);
                }

                if (claseDTO.getDocenteSuplente() != null) {
                    Persona suplente = personaRepository.findById(claseDTO.getDocenteSuplente())
                            .orElseThrow(() -> new RuntimeException("Docente suplente no encontrado con id: " + claseDTO.getDocenteSuplente()));
                    clase.setDocenteSuplente(suplente);
                } else {
                    clase.setDocenteSuplente(null);
                }

                auditar(clase, username);
            }
        } else {
            // Si no vienen clases en el DTO, eliminar todas
            clasesExistentes.clear();
        }

        comisionRepository.save(comision);
        return toResponseDTO(comision);
    }

    @Transactional
    public void desactivar(Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Comision comision = comisionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comision no encontrada con id: " + id));

        comision.setComActiva(false);
        auditar(comision, username);
        comisionRepository.save(comision);
    }

    @Transactional
    public void activar(Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Comision comision = comisionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comision no encontrada con id: " + id));

        comision.setComActiva(true);
        auditar(comision, username);
        comisionRepository.save(comision);
    }

    @Transactional
    public void duplicarPorAnio(Integer anioDesde, Integer anioHasta) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        List<Comision> comisiones = comisionRepository.findByComAnioOrderByCursoCurNombreAscComNombreAsc(anioDesde).stream()
                .filter(Comision::getComActiva)
                .toList();

        for (Comision original : comisiones) {
            Comision nueva = new Comision();
            nueva.setCurso(original.getCurso());
            nueva.setComAnio(anioHasta);
            nueva.setComNombre(original.getComNombre());
            nueva.setComCupo(original.getComCupo());
            nueva.setComActiva(true);
            auditar(nueva, username);

            List<ComisionClase> clases = new ArrayList<>();
            if (original.getComisionesClases() != null) {
                for (ComisionClase originalClase : original.getComisionesClases()) {
                    ComisionClase nuevaClase = new ComisionClase();
                    nuevaClase.setComision(nueva);
                    nuevaClase.setClcDiaSemana(originalClase.getClcDiaSemana());
                    nuevaClase.setClcHoraDesde(originalClase.getClcHoraDesde());
                    nuevaClase.setClcHoraHasta(originalClase.getClcHoraHasta());
                    nuevaClase.setDocente(originalClase.getDocente());
                    nuevaClase.setDocenteSuplente(originalClase.getDocenteSuplente());
                    auditar(nuevaClase, username);
                    clases.add(nuevaClase);
                }
            }
            nueva.setComisionesClases(clases);
            comisionRepository.save(nueva);
        }
    }

    @Transactional(readOnly = true)
    public ComisionDTO obtenerPorId(Long id) {
        Comision comision = comisionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comisión no encontrada con id: " + id));
        return toResponseDTO(comision);
    }

    @Transactional(readOnly = true)
    public List<ComisionDTO> listarActivas() {
        return comisionRepository.findAllActivas().stream()
                .map(c -> {
                    ComisionDTO dto = new ComisionDTO();
                    dto.setComId(c.getComId());
                    dto.setNombre(c.getComNombre());
                    dto.setCursoNombre(c.getCurso().getCurNombre());
                    return dto;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Integer> obtenerAniosDisponibles() {
        return comisionRepository.findDistinctAnios();
    }

    @Transactional(readOnly = true)
    public List<ComisionDTO> buscarPorAnio(Integer anio) {
        return comisionRepository.findByComAnioOrderByCursoCurNombreAscComNombreAsc(anio).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ComisionDTO> buscarPorCurso(Long cursoId) {
        return comisionRepository.findByCursoCurId(cursoId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    private ComisionDTO toResponseDTO(Comision comision) {
        ComisionDTO dto = new ComisionDTO();
        dto.setComId(comision.getComId());
        dto.setCursoId(comision.getCurso().getCurId());
        dto.setCursoNombre(comision.getCurso().getCurNombre());
        dto.setAnio(comision.getComAnio());
        dto.setNombre(comision.getComNombre());
        dto.setCupo(comision.getComCupo());
        dto.setActiva(comision.getComActiva());

        long inscriptos = 0;
        long preInscriptos = 0;
        if (comision.getInscripciones() != null) {
            inscriptos = comision.getInscripciones().stream()
                    .filter(i -> "ACTIVA".equals(i.getInsEstado()))
                    .count();
            preInscriptos = comision.getInscripciones().stream()
                    .filter(i -> "INACTIVA".equals(i.getInsEstado()))
                    .count();
        }
        dto.setInscriptos(inscriptos);
        dto.setPreInscriptos(preInscriptos);

        if(comision.getComisionesClases() != null){
            dto.setClases(comision.getComisionesClases().stream().map(ComisionClaseDTO::new).toList());
        }

        return dto;
    }

}
