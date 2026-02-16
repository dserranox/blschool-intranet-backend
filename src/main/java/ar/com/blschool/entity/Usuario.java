package ar.com.blschool.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "USUARIOS")
public class Usuario implements IAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USR_ID")
    private Long id;

    @Column(name = "USR_USERNAME", nullable = false, length = 200)
    private String username;

    @Column(name = "USR_PASSWORD", nullable = false)
    private String password;

    @Column(name = "USR_ACTIVO", nullable = false)
    private Boolean activo = true;

    @Column(name = "USR_ULT_LOGIN")
    private LocalDateTime ultimoLogin;

    @Column(name = "USR_PER_ID")
    private Long personaId;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "USUARIOS_ROLES",
            joinColumns = @JoinColumn(name = "UR_USR_ID"),
            inverseJoinColumns = @JoinColumn(name = "UR_ROL_ID"))
    private Set<Rol> roles = new HashSet<>();

    @Column(name = "AUD_USR_INS", nullable = false, length = 250)
    private String audUsrIns;
    @Column(name = "AUD_USR_UPD", nullable = false, length = 250)
    private String audUsrUpd;
    @Column(name = "AUD_FECHA_INS", nullable = false)
    private LocalDateTime audFechaIns;
    @Column(name = "AUD_FECHA_UPD", nullable = false)
    private LocalDateTime audFechaUpd;
}
