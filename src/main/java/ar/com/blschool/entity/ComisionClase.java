package ar.com.blschool.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "comision_clases")
public class ComisionClase implements IAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "clc_id")
    private Long clcId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clc_com_id", nullable = false)
    private Comision comision;

    @Column(name = "clc_dia_semana", nullable = false)
    private Integer clcDiaSemana;

    @Column(name = "clc_hora_desde", nullable = false)
    private LocalTime clcHoraDesde;

    @Column(name = "clc_hora_hasta", nullable = false)
    private LocalTime clcHoraHasta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clc_docente_id")
    private Persona docente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clc_docente_id_suplente")
    private Persona docenteSuplente;

    @Column(name = "AUD_USR_INS", nullable = false, length = 250)
    private String audUsrIns;
    @Column(name = "AUD_USR_UPD", nullable = false, length = 250)
    private String audUsrUpd;
    @Column(name = "AUD_FECHA_INS", nullable = false, length = 7)
    private LocalDateTime audFechaIns;
    @Column(name = "AUD_FECHA_UPD", nullable = false, length = 7)
    private LocalDateTime audFechaUpd;
}
