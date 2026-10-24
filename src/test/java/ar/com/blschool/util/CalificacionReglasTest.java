package ar.com.blschool.util;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static ar.com.blschool.util.CalificacionReglas.*;
import static org.assertj.core.api.Assertions.*;

class CalificacionReglasTest {

    private static BigDecimal bd(String v) {
        return new BigDecimal(v);
    }

    @Test
    void logrosQueSumanCienSonValidos() {
        assertThatCode(() -> validarLogros(
                List.of("Listening", "Vocabulary and Grammar", "Speaking"),
                List.of(bd("25"), bd("50"), bd("25")))).doesNotThrowAnyException();
        assertThatCode(() -> validarLogros(
                List.of("A", "B", "C"), List.of(bd("33.33"), bd("33.33"), bd("33.34")))).doesNotThrowAnyException();
    }

    @Test
    void listaVaciaDeLogrosEsValida() {
        assertThatCode(() -> validarLogros(List.of(), List.of())).doesNotThrowAnyException();
    }

    @Test
    void logrosQueNoSumanCienSonRechazados() {
        assertThatThrownBy(() -> validarLogros(List.of("A", "B"), List.of(bd("40"), bd("50"))))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("suman 90");
    }

    @Test
    void logrosConNombreVacioRepetidoOPesoInvalidoSonRechazados() {
        assertThatThrownBy(() -> validarLogros(List.of(" ", "B"), List.of(bd("50"), bd("50"))))
                .hasMessageContaining("no tiene nombre");
        assertThatThrownBy(() -> validarLogros(List.of("Reading", "reading"), List.of(bd("50"), bd("50"))))
                .hasMessageContaining("repetido");
        assertThatThrownBy(() -> validarLogros(List.of("A", "B"), List.of(bd("0"), bd("100"))))
                .hasMessageContaining("mayor a 0");
    }

    @Test
    void nombreDeLogroMuyLargoEsRechazado() {
        String largo = "x".repeat(101);
        assertThatThrownBy(() -> validarLogros(List.of(largo), List.of(bd("100"))))
                .hasMessageContaining("supera los 100");
    }

    @Test
    void pesoSeRedondeaAntesDeValidarRango() {
        assertThatCode(() -> validarLogros(List.of("A"), List.of(bd("100.004")))).doesNotThrowAnyException();
    }

    @Test
    void aporteDebeEstarEntreCeroYElPeso() {
        assertThatCode(() -> validarAporte("Perez, Ana", "Listening", bd("25"), bd("25"))).doesNotThrowAnyException();
        assertThatCode(() -> validarAporte("Perez, Ana", "Listening", null, bd("25"))).doesNotThrowAnyException();
        assertThatThrownBy(() -> validarAporte("Perez, Ana", "Listening", bd("25.01"), bd("25")))
                .hasMessageContaining("Perez, Ana")
                .hasMessageContaining("Listening")
                .hasMessageContaining("entre 0 y 25");
        assertThatThrownBy(() -> validarAporte("Perez, Ana", "Listening", bd("-1"), bd("25")))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void notaDePruebaDebeEstarEntreCeroYCien() {
        assertThatCode(() -> validarNotaPrueba("Perez, Ana", "Test 1", bd("100"))).doesNotThrowAnyException();
        assertThatThrownBy(() -> validarNotaPrueba("Perez, Ana", "Test 1", bd("100.5")))
                .hasMessageContaining("Test 1");
    }

    @Test
    void notaEsLaSumaSoloSiEstanTodosLosLogros() {
        List<Long> logros = List.of(1L, 2L, 3L);
        assertThat(calcularNota(logros, Map.of(1L, bd("22.22"), 2L, bd("47.25"), 3L, bd("23.75"))))
                .isEqualByComparingTo("93.22");
        assertThat(calcularNota(logros, Map.of(1L, bd("22.22"), 2L, bd("47.25")))).isNull();
    }

    @Test
    void etapaSeProponePorFecha() {
        assertThat(etapaPorFecha(LocalDate.of(2026, 7, 31))).isEqualTo(1);
        assertThat(etapaPorFecha(LocalDate.of(2026, 8, 1))).isEqualTo(2);
    }

    @Test
    void redondeaADosDecimales() {
        assertThat(redondear(bd("22.225"))).isEqualByComparingTo("22.23");
        assertThat(redondear(null)).isNull();
    }

    @Test
    void pruebasPorLogrosSoloEnYearYFirst() {
        assertThat(pruebasPorLogros("Year 1")).isTrue();
        assertThat(pruebasPorLogros("FIRST Certificate")).isTrue();
        assertThat(pruebasPorLogros("Kids 2")).isFalse();
        assertThat(pruebasPorLogros(null)).isFalse();
    }

    @Test
    void finalMarkManualEsExcluyenteConLosLogros() {
        assertThatCode(() -> validarNotaManual("Pérez, Ana", bd("80"), false)).doesNotThrowAnyException();
        assertThatCode(() -> validarNotaManual("Pérez, Ana", null, true)).doesNotThrowAnyException();
        assertThatThrownBy(() -> validarNotaManual("Pérez, Ana", bd("80"), true))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("no ambos");
    }

    @Test
    void finalMarkManualFueraDeRangoEsRechazado() {
        assertThatThrownBy(() -> validarNotaManual("Pérez, Ana", bd("100.01"), false))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("entre 0 y 100");
        assertThatThrownBy(() -> validarNotaManual("Pérez, Ana", bd("-1"), false))
                .isInstanceOf(ResponseStatusException.class);
    }
}
