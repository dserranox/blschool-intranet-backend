package ar.com.blschool.entity;

import jakarta.persistence.*;
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
@Table(name = "evaluacion_logros")
public class EvaluacionLogro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "elo_id")
    private Long eloId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "elo_eva_id", nullable = false)
    private Evaluacion evaluacion;

    @Column(name = "elo_nombre", nullable = false, length = 100)
    private String eloNombre;

    @Column(name = "elo_peso", nullable = false, precision = 5, scale = 2)
    private BigDecimal eloPeso;

    @Column(name = "elo_orden", nullable = false)
    private Integer eloOrden;
}
