package ar.com.blschool.util;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Reglas del módulo de calificaciones. Los errores se lanzan como 400 con un mensaje
 * pensado para mostrarse tal cual a la docente.
 */
public final class CalificacionReglas {

    public static final BigDecimal CIEN = new BigDecimal("100.00");

    private CalificacionReglas() {}

    public static BigDecimal redondear(BigDecimal valor) {
        return valor == null ? null : valor.setScale(2, RoundingMode.HALF_UP);
    }

    /** Logros de un curso: nombres no vacíos ni repetidos, pesos en (0, 100] que suman 100. Lista vacía = sin configuración. */
    public static void validarLogros(List<String> nombres, List<BigDecimal> pesos) {
        if (nombres.isEmpty()) {
            return;
        }
        Set<String> vistos = new HashSet<>();
        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < nombres.size(); i++) {
            String nombre = nombres.get(i) == null ? "" : nombres.get(i).trim();
            BigDecimal peso = redondear(pesos.get(i));
            if (nombre.isEmpty()) {
                throw error("El logro " + (i + 1) + " no tiene nombre");
            }
            if (nombre.length() > 100) {
                throw error("El nombre del logro '" + nombre + "' supera los 100 caracteres");
            }
            if (!vistos.add(nombre.toLowerCase())) {
                throw error("El logro '" + nombre + "' está repetido");
            }
            if (peso == null || peso.compareTo(BigDecimal.ZERO) <= 0 || peso.compareTo(CIEN) > 0) {
                throw error("El peso de '" + nombre + "' debe ser mayor a 0 y hasta 100");
            }
            total = total.add(peso);
        }
        if (total.compareTo(CIEN) != 0) {
            throw error("Los pesos deben sumar 100 (suman " + total.stripTrailingZeros().toPlainString() + ")");
        }
    }

    /** Aporte de un logro: entre 0 y su peso. null = sin cargar (válido). */
    public static void validarAporte(String alumno, String logro, BigDecimal valor, BigDecimal peso) {
        if (valor == null) {
            return;
        }
        if (valor.compareTo(BigDecimal.ZERO) < 0 || redondear(valor).compareTo(peso) > 0) {
            throw error(alumno + ": " + logro + " debe estar entre 0 y " + peso.stripTrailingZeros().toPlainString());
        }
    }

    /** Nota de prueba: entre 0 y 100. null = sin cargar (válido). */
    public static void validarNotaPrueba(String alumno, String prueba, BigDecimal nota) {
        if (nota == null) {
            return;
        }
        if (nota.compareTo(BigDecimal.ZERO) < 0 || redondear(nota).compareTo(CIEN) > 0) {
            throw error(alumno + ": la nota de '" + prueba + "' debe estar entre 0 y 100");
        }
    }

    /** Final Mark = suma de aportes, sólo si están cargados todos los logros; si falta alguno, null. */
    public static BigDecimal calcularNota(Collection<Long> idsLogros, Map<Long, BigDecimal> valores) {
        BigDecimal total = BigDecimal.ZERO;
        for (Long id : idsLogros) {
            BigDecimal valor = valores.get(id);
            if (valor == null) {
                return null;
            }
            total = total.add(redondear(valor));
        }
        return redondear(total);
    }

    /** Etapa sugerida para una prueba: enero-julio = 1, agosto-diciembre = 2. */
    public static int etapaPorFecha(LocalDate fecha) {
        return fecha.getMonthValue() <= 7 ? 1 : 2;
    }

    private static ResponseStatusException error(String mensaje) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensaje);
    }
}
