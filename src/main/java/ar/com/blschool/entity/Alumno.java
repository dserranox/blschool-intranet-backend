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
@Table(name = "alumnos")
public class Alumno implements IAuditable{

    @Id
    @Column(name = "alu_per_id")
    private Long aluId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "alu_per_id")
    private Persona persona;

    @Column(name = "alu_escuela", length = 200)
    private String aluEscuela;

    @Column(name = "alu_grado_curso", length = 100)
    private String aluGradoCurso;

    @Column(name = "alu_email_alternativo", length = 200)
    private String aluEmailAlternativo;

    @Column(name = "alu_estado", length = 100)
    private String aluEstado;

    @Column(name = "AUD_USR_INS", nullable = false, length = 250)
    private String audUsrIns;
    @Column(name = "AUD_USR_UPD", nullable = false, length = 250)
    private String audUsrUpd;
    @Column(name = "AUD_FECHA_INS", nullable = false, length = 7)
    private LocalDateTime audFechaIns;
    @Column(name = "AUD_FECHA_UPD", nullable = false, length = 7)
    private LocalDateTime audFechaUpd;
}
