package ar.com.blschool.entity;

import jakarta.persistence.*;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Audited
@Table(name = "personas")
public class Persona implements IAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "per_id")
    private Long perId;

    @Column(name = "per_nombres", nullable = false, length = 150)
    private String perNombres;

    @Column(name = "per_apellido", nullable = false, length = 150)
    private String perApellido;

    @Column(name = "per_fecha_nacimiento")
    private LocalDate perFechaNacimiento;

    @Column(name = "per_dni", length = 20, unique = true)
    private String perDni;

    @Column(name = "per_email", length = 200)
    private String perEmail;

    @Column(name = "per_direccion", length = 250)
    private String perDireccion;

    // Los campos de auditoría propios no se versionan: guardar sin cambios reales no genera revisión
    @NotAudited
    @Column(name = "AUD_USR_INS", nullable = false, length = 250)
    private String audUsrIns;
    @NotAudited
    @Column(name = "AUD_USR_UPD", nullable = false, length = 250)
    private String audUsrUpd;
    @NotAudited
    @Column(name = "AUD_FECHA_INS", nullable = false, length = 7)
    private LocalDateTime audFechaIns;
    @NotAudited
    @Column(name = "AUD_FECHA_UPD", nullable = false, length = 7)
    private LocalDateTime audFechaUpd;

    @OneToMany(mappedBy = "persona", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PersonaTelefono> personasTelefonos;
}
