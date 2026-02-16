package ar.com.blschool.service;

import ar.com.blschool.dto.DocenteDTO;
import ar.com.blschool.dto.PersonaTelefonoDTO;
import ar.com.blschool.entity.Persona;
import ar.com.blschool.entity.PersonaTelefono;
import ar.com.blschool.entity.Rol;
import ar.com.blschool.entity.Usuario;
import ar.com.blschool.repository.PersonaRepository;
import ar.com.blschool.repository.RolRepository;
import ar.com.blschool.repository.UsuarioRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class DocenteService extends BaseService {

    private final PersonaRepository personaRepository;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public DocenteService(PersonaRepository personaRepository,
                          UsuarioRepository usuarioRepository,
                          RolRepository rolRepository,
                          PasswordEncoder passwordEncoder) {
        this.personaRepository = personaRepository;
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public DocenteDTO crear(DocenteDTO dto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        if (usuarioRepository.existsByUsernameIgnoreCase(dto.getUsuario())) {
            throw new RuntimeException("Ya existe un usuario con el nombre: " + dto.getUsuario());
        }

        // 1. Crear Persona
        Persona persona = new Persona();
        persona.setPerNombres(dto.getNombres());
        persona.setPerApellido(dto.getApellidos());
        persona.setPerDni(dto.getDni());
        persona.setPerEmail(dto.getEmail());
        persona.setPerDireccion(dto.getDireccion());
        auditar(persona, username);

        // 2. Crear PersonaTelefono asociado
        List<PersonaTelefono> telefonos = new ArrayList<>();
        if (dto.getTelefono() != null) {
            PersonaTelefono telefono = new PersonaTelefono();
            telefono.setPersona(persona);
            telefono.setPteNumero(dto.getTelefono());
            telefono.setPteTipo("DOCENTE");
            telefono.setPtePrincipal(true);
            auditar(telefono, username);
            telefonos.add(telefono);
        }
        persona.setPersonasTelefonos(telefonos);

        personaRepository.save(persona);

        // 3. Crear Usuario con password encriptada
        Rol rol = rolRepository.findByNombreIgnoreCase(dto.getRol())
                .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + dto.getRol()));

        Usuario usuario = new Usuario();
        usuario.setUsername(dto.getUsuario());
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setActivo(true);
        usuario.setPersonaId(persona.getPerId());
        usuario.getRoles().add(rol);
        auditar(usuario, username);

        usuarioRepository.save(usuario);

        // 4. Retornar DTO con ID generado
        dto.setId(persona.getPerId());
        dto.setPassword(null);
        return dto;
    }

    @Transactional(readOnly = true)
    public List<DocenteDTO> listar() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // Determinar si el usuario logueado es ADMIN
        List<String> roles = new ArrayList<>();
        roles.add("DOCENTE");

        Usuario usuarioLogueado = usuarioRepository.findByUsernameIgnoreCase(username).orElse(null);
        if (usuarioLogueado != null) {
            boolean esAdmin = usuarioLogueado.getRoles().stream()
                    .anyMatch(r -> "ADMIN".equalsIgnoreCase(r.getNombre()));
            if (esAdmin) {
                roles.add("ADMIN");
            }
        }

        return personaRepository.findByRoles(roles).stream()
                .map(this::toListDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public DocenteDTO obtenerPorId(Long personaId) {
        Persona persona = personaRepository.findById(personaId)
                .orElseThrow(() -> new RuntimeException("Persona no encontrada con id: " + personaId));

        DocenteDTO dto = toListDTO(persona);

        // Telefonos
        if (persona.getPersonasTelefonos() != null) {
            dto.setTelefonos(persona.getPersonasTelefonos().stream()
                    .map(PersonaTelefonoDTO::new)
                    .toList());
        }

        // Usuario asociado
        usuarioRepository.findByPersonaId(personaId)
                .ifPresent(u -> dto.setUsuario(u.getUsername()));

        return dto;
    }

    @Transactional
    public DocenteDTO modificar(Long personaId, DocenteDTO dto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // Validar permisos: solo el propio usuario o un ADMIN puede modificar
        Usuario usuarioLogueado = usuarioRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new RuntimeException("Usuario logueado no encontrado"));

        boolean esAdmin = usuarioLogueado.getRoles().stream()
                .anyMatch(r -> "ADMIN".equalsIgnoreCase(r.getNombre()));
        boolean esMismoUsuario = personaId.equals(usuarioLogueado.getPersonaId());

        if (!esAdmin && !esMismoUsuario) {
            throw new AccessDeniedException("No tiene permisos para modificar este docente");
        }

        // 1. Actualizar Persona
        Persona persona = personaRepository.findById(personaId)
                .orElseThrow(() -> new RuntimeException("Persona no encontrada con id: " + personaId));

        persona.setPerNombres(dto.getNombres());
        persona.setPerApellido(dto.getApellidos());
        persona.setPerDni(dto.getDni());
        persona.setPerEmail(dto.getEmail());
        persona.setPerDireccion(dto.getDireccion());
        auditar(persona, username);

        // 2. Actualizar teléfono principal
        if (dto.getTelefono() != null) {
            PersonaTelefono principal = null;
            if (persona.getPersonasTelefonos() != null) {
                principal = persona.getPersonasTelefonos().stream()
                        .filter(t -> Boolean.TRUE.equals(t.getPtePrincipal()))
                        .findFirst()
                        .orElse(null);
            }
            if (principal != null) {
                principal.setPteNumero(dto.getTelefono());
                auditar(principal, username);
            } else {
                if (persona.getPersonasTelefonos() == null) {
                    persona.setPersonasTelefonos(new ArrayList<>());
                }
                PersonaTelefono nuevo = new PersonaTelefono();
                nuevo.setPersona(persona);
                nuevo.setPteNumero(dto.getTelefono());
                nuevo.setPteTipo("DOCENTE");
                nuevo.setPtePrincipal(true);
                auditar(nuevo, username);
                persona.getPersonasTelefonos().add(nuevo);
            }
        }

        personaRepository.save(persona);

        // 3. Actualizar Usuario
        Usuario usuario = usuarioRepository.findByPersonaId(personaId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado para persona id: " + personaId));

        if (dto.getUsuario() != null) {
            // Validar que el nuevo username no esté en uso por otro usuario
            if (!usuario.getUsername().equalsIgnoreCase(dto.getUsuario())
                    && usuarioRepository.existsByUsernameIgnoreCase(dto.getUsuario())) {
                throw new RuntimeException("Ya existe un usuario con el nombre: " + dto.getUsuario());
            }
            usuario.setUsername(dto.getUsuario());
        }

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        if (dto.getRol() != null) {
            Rol rol = rolRepository.findByNombreIgnoreCase(dto.getRol())
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + dto.getRol()));
            usuario.getRoles().clear();
            usuario.getRoles().add(rol);
        }

        auditar(usuario, username);
        usuarioRepository.save(usuario);

        // 4. Retornar DTO
        dto.setId(personaId);
        dto.setPassword(null);
        return dto;
    }

    private DocenteDTO toListDTO(Persona persona) {
        DocenteDTO dto = new DocenteDTO();
        dto.setId(persona.getPerId());
        dto.setNombres(persona.getPerNombres());
        dto.setApellidos(persona.getPerApellido());
        dto.setDni(persona.getPerDni());
        dto.setDireccion(persona.getPerDireccion());
        dto.setEmail(persona.getPerEmail());

        // Primer teléfono encontrado
        if (persona.getPersonasTelefonos() != null && !persona.getPersonasTelefonos().isEmpty()) {
            dto.setTelefono(persona.getPersonasTelefonos().getFirst().getPteNumero());
        }

        return dto;
    }
}
