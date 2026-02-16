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
@Table(name = "personas_telefonos")
public class PersonaTelefono implements IAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pte_id")
    private Long pteId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pte_per_id", nullable = false)
    private Persona persona;

    @Column(name = "pte_numero", nullable = false, length = 30)
    private String pteNumero;

    @Column(name = "pte_tipo", nullable = false, length = 30)
    private String pteTipo;

    @Column(name = "pte_nota", length = 100)
    private String pteNota;

    @Column(name = "pte_principal", nullable = false)
    private Boolean ptePrincipal;

    @Column(name = "AUD_USR_INS", nullable = false, length = 250)
    private String audUsrIns;
    @Column(name = "AUD_USR_UPD", nullable = false, length = 250)
    private String audUsrUpd;
    @Column(name = "AUD_FECHA_INS", nullable = false, length = 7)
    private LocalDateTime audFechaIns;
    @Column(name = "AUD_FECHA_UPD", nullable = false, length = 7)
    private LocalDateTime audFechaUpd;
}
