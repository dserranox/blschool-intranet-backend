package ar.com.blschool.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "evaluaciones")
public class Evaluacion implements IAuditable {

    public static final String PARCIAL = "PARCIAL";
    public static final String FINAL = "FINAL";
    public static final String PRUEBA = "PRUEBA";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "eva_id")
    private Long evaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "eva_com_id", nullable = false)
    private Comision comision;

    @Column(name = "eva_tipo", nullable = false, length = 10)
    private String evaTipo;

    @Column(name = "eva_nombre", nullable = false, length = 100)
    private String evaNombre;

    @Column(name = "eva_fecha")
    private LocalDate evaFecha;

    @Column(name = "eva_etapa")
    private Integer evaEtapa;

    /** Copia congelada de los logros del curso (sólo Parcial/Final). */
    @OneToMany(mappedBy = "evaluacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("eloOrden ASC")
    private List<EvaluacionLogro> logros = new ArrayList<>();

    @Column(name = "AUD_USR_INS", nullable = false, length = 250)
    private String audUsrIns;
    @Column(name = "AUD_USR_UPD", nullable = false, length = 250)
    private String audUsrUpd;
    @Column(name = "AUD_FECHA_INS", nullable = false, length = 7)
    private LocalDateTime audFechaIns;
    @Column(name = "AUD_FECHA_UPD", nullable = false, length = 7)
    private LocalDateTime audFechaUpd;
}
