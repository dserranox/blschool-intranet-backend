package ar.com.blschool.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "logros")
public class Logro implements IAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "log_cur_id", nullable = false)
    private Curso curso;

    @Column(name = "log_nombre", nullable = false, length = 100)
    private String logNombre;

    @Column(name = "log_peso", nullable = false, precision = 5, scale = 2)
    private BigDecimal logPeso;

    @Column(name = "log_orden", nullable = false)
    private Integer logOrden;

    @Column(name = "AUD_USR_INS", nullable = false, length = 250)
    private String audUsrIns;
    @Column(name = "AUD_USR_UPD", nullable = false, length = 250)
    private String audUsrUpd;
    @Column(name = "AUD_FECHA_INS", nullable = false, length = 7)
    private LocalDateTime audFechaIns;
    @Column(name = "AUD_FECHA_UPD", nullable = false, length = 7)
    private LocalDateTime audFechaUpd;
}
