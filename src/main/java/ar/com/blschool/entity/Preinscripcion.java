package ar.com.blschool.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "preinscripciones")
public class Preinscripcion implements IAuditable {

    public static final String PENDIENTE = "PENDIENTE";
    public static final String ASIGNADA = "ASIGNADA";
    public static final String DESCARTADA = "DESCARTADA";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pre_id")
    private Long preId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pre_alu_per_id", nullable = false)
    private Alumno alumno;

    @Column(name = "pre_anio", nullable = false)
    private Integer preAnio;

    @Column(name = "pre_fecha_formulario")
    private LocalDateTime preFechaFormulario;

    @Column(name = "pre_escuela_curso_turno", length = 300)
    private String preEscuelaCursoTurno;

    @Column(name = "pre_alumno_nuevo", nullable = false)
    private Boolean preAlumnoNuevo;

    @Column(name = "pre_estado", nullable = false, length = 20)
    private String preEstado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pre_com_id")
    private Comision comision;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pre_ins_id")
    private Inscripcion inscripcion;

    @Column(name = "pre_origen", nullable = false, length = 30)
    private String preOrigen;

    @Column(name = "pre_datos_formulario")
    private String preDatosFormulario;

    @Column(name = "AUD_USR_INS", nullable = false, length = 250)
    private String audUsrIns;
    @Column(name = "AUD_USR_UPD", nullable = false, length = 250)
    private String audUsrUpd;
    @Column(name = "AUD_FECHA_INS", nullable = false, length = 7)
    private LocalDateTime audFechaIns;
    @Column(name = "AUD_FECHA_UPD", nullable = false, length = 7)
    private LocalDateTime audFechaUpd;
}
