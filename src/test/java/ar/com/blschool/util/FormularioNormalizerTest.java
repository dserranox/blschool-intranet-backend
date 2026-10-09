package ar.com.blschool.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static ar.com.blschool.util.FormularioNormalizer.*;
import static org.assertj.core.api.Assertions.assertThat;

class FormularioNormalizerTest {

    @Test
    void normalizaDniConPuntosEspaciosYGuiones() {
        assertThat(normalizarDni("57.876.116")).isEqualTo("57876116");
        assertThat(normalizarDni("54.451.414 ")).isEqualTo("54451414");
        assertThat(normalizarDni("57.876112")).isEqualTo("57876112");
        assertThat(normalizarDni("58-155-446")).isEqualTo("58155446");
        assertThat(normalizarDni("5815544")).isEqualTo("5815544");
    }

    @Test
    void dniVacioOInvalidoDevuelveNull() {
        assertThat(normalizarDni(null)).isNull();
        assertThat(normalizarDni("")).isNull();
        assertThat(normalizarDni("  ")).isNull();
        assertThat(normalizarDni("123")).isNull();
        assertThat(normalizarDni("no tiene")).isNull();
    }

    @Test
    void normalizaTelefono() {
        assertThat(normalizarTelefono("03564 15617009")).isEqualTo("0356415617009");
        assertThat(normalizarTelefono("3564 631884")).isEqualTo("3564631884");
        assertThat(normalizarTelefono("(3564) 58-2189")).isEqualTo("3564582189");
        assertThat(normalizarTelefono("")).isNull();
        assertThat(normalizarTelefono("-")).isNull();
    }

    @Test
    void separaNombreYApellidoTomandoLaUltimaPalabraComoApellido() {
        assertThat(separarNombreApellido("Tomás Armando")).containsExactly("Tomás", "Armando");
        assertThat(separarNombreApellido("  JUAN IGNACIO   PERALTA ")).containsExactly("JUAN IGNACIO", "PERALTA");
        assertThat(separarNombreApellido("Felipe")).containsExactly("Felipe", "");
        assertThat(separarNombreApellido(null)).containsExactly("", "");
    }

    @Test
    void parseaFechasDelFormulario() {
        assertThat(parsearFecha("3/11/2020")).isEqualTo(LocalDate.of(2020, 11, 3));
        assertThat(parsearFecha("2020-11-03")).isEqualTo(LocalDate.of(2020, 11, 3));
        assertThat(parsearFecha("03-11-2020")).isEqualTo(LocalDate.of(2020, 11, 3));
        assertThat(parsearFecha("ayer")).isNull();

        assertThat(parsearFechaHora("13/10/2025 9:37:10")).isEqualTo(LocalDateTime.of(2025, 10, 13, 9, 37, 10));
        assertThat(parsearFechaHora("2025-10-13T09:37:10")).isEqualTo(LocalDateTime.of(2025, 10, 13, 9, 37, 10));
    }

    @Test
    void interpretaSiNo() {
        assertThat(parsearSiNo("Sí")).isTrue();
        assertThat(parsearSiNo("si")).isTrue();
        assertThat(parsearSiNo("No")).isFalse();
        assertThat(parsearSiNo("")).isNull();
    }
}
