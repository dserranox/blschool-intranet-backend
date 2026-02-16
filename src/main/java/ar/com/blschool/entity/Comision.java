package ar.com.blschool.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "comisiones")
public class Comision implements IAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "com_id")
    private Long comId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "com_cur_id", nullable = false)
    private Curso curso;

    @Column(name = "com_anio", nullable = false)
    private Integer comAnio;

    @Column(name = "com_nombre", nullable = false, length = 100)
    private String comNombre;

    @Column(name = "com_cupo", nullable = false)
    private Integer comCupo;

    @Column(name = "com_activa", nullable = false)
    private Boolean comActiva;

    @Column(name = "AUD_USR_INS", nullable = false, length = 250)
    private String audUsrIns;
    @Column(name = "AUD_USR_UPD", nullable = false, length = 250)
    private String audUsrUpd;
    @Column(name = "AUD_FECHA_INS", nullable = false, length = 7)
    private LocalDateTime audFechaIns;
    @Column(name = "AUD_FECHA_UPD", nullable = false, length = 7)
    private LocalDateTime audFechaUpd;

    @OneToMany(mappedBy = "comision", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ComisionClase> comisionesClases;

    @JsonIgnore
    @OneToMany(mappedBy = "comision")
    private List<Inscripcion> inscripciones;
}
