package ar.com.blschool.entity;

import ar.com.blschool.auditoria.RevisionUsuarioListener;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

import java.time.LocalDateTime;

/**
 * Una fila por transacción que modificó alguna entidad auditada (@Audited).
 * Las tablas *_AUD referencian esta revisión por la columna REV.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "revisiones")
@RevisionEntity(RevisionUsuarioListener.class)
public class Revision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @RevisionNumber
    @Column(name = "rev_id")
    private Long revId;

    @RevisionTimestamp
    @Column(name = "rev_fecha", nullable = false)
    private LocalDateTime revFecha;

    @Column(name = "rev_usuario", nullable = false, length = 250)
    private String revUsuario;
}
