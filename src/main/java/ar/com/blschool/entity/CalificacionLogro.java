package ar.com.blschool.entity;

import jakarta.persistence.*;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Audited
@Table(name = "calificacion_logros")
public class CalificacionLogro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "clo_id")
    private Long cloId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clo_cal_id", nullable = false)
    private Calificacion calificacion;

    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clo_elo_id", nullable = false)
    private EvaluacionLogro evaluacionLogro;

    @Column(name = "clo_valor", nullable = false, precision = 5, scale = 2)
    private BigDecimal cloValor;
}
