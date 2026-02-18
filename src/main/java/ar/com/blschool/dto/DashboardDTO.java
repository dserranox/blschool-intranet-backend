package ar.com.blschool.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDTO {
    private long alumnosActivos;
    private long docentesActivos;
    private long cursosActivos;
    private long comisionesActivas;
}
