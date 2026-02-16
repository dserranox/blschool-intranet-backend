package ar.com.blschool.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Table(name = "inscripciones")
public class Inscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ins_id")
    private Long insId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ins_alu_per_id", nullable = false)
    private Alumno alumno;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ins_com_id", nullable = false)
    private Comision comision;

    @Column(name = "ins_fecha", nullable = false)
    private LocalDateTime insFecha;

    @Column(name = "ins_estado", nullable = false, length = 20)
    private String insEstado;
}
