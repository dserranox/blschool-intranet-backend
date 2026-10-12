package ar.com.blschool.entity;

import jakarta.persistence.*;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RelationTargetAuditMode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Audited
@Table(name = "calificaciones")
public class Calificacion implements IAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cal_id")
    private Long calId;

    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cal_eva_id", nullable = false)
    private Evaluacion evaluacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cal_alu_per_id", nullable = false)
    private Alumno alumno;

    /** Prueba: porcentaje. Parcial/Final: suma de aportes (null si incompleta o ausente). */
    @Column(name = "cal_nota", precision = 5, scale = 2)
    private BigDecimal calNota;

    @Column(name = "cal_ausente", nullable = false)
    private Boolean calAusente;

    @OneToMany(mappedBy = "calificacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @org.hibernate.annotations.BatchSize(size = 50)
    private List<CalificacionLogro> valores = new ArrayList<>();

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
}
