package ar.com.blschool.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.text.Normalizer;
import java.util.List;

/**
 * Normalización de los datos que llegan desde el Google Form de preinscripción.
 * Los datos son texto libre cargado por las familias, por eso todo es tolerante:
 * lo que no se puede interpretar se devuelve como null.
 */
public final class FormularioNormalizer {

    private static final List<DateTimeFormatter> FORMATOS_FECHA = List.of(
            DateTimeFormatter.ofPattern("d/M/uuuu"),
            DateTimeFormatter.ofPattern("d-M-uuuu"),
            DateTimeFormatter.ofPattern("d.M.uuuu"),
            DateTimeFormatter.ISO_LOCAL_DATE
    );

    private static final List<DateTimeFormatter> FORMATOS_FECHA_HORA = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ofPattern("d/M/uuuu H:mm:ss"),
            DateTimeFormatter.ofPattern("d/M/uuuu H:mm")
    );

    private FormularioNormalizer() {}

    /** Deja sólo los dígitos: "57.876.116 " -> "57876116". */
    public static String soloDigitos(String valor) {
        if (valor == null) {
            return null;
        }
        String digitos = valor.replaceAll("\\D", "");
        return digitos.isEmpty() ? null : digitos;
    }

    /** DNI normalizado (7 a 9 dígitos) o null si no es válido. */
    public static String normalizarDni(String dni) {
        String digitos = soloDigitos(dni);
        if (digitos == null) {
            return null;
        }
        // Un DNI cargado como número en la planilla puede venir con ceros a la izquierda perdidos o agregados
        digitos = digitos.replaceFirst("^0+(?=\\d{7})", "");
        return digitos.length() >= 7 && digitos.length() <= 9 ? digitos : null;
    }

    /** Teléfono sólo con dígitos; null si queda vacío o es demasiado corto para ser un teléfono. */
    public static String normalizarTelefono(String telefono) {
        String digitos = soloDigitos(telefono);
        return digitos != null && digitos.length() >= 6 ? digitos : null;
    }

    /** Trim + espacios múltiples colapsados; null si queda vacío. */
    public static String limpiar(String valor) {
        if (valor == null) {
            return null;
        }
        String limpio = valor.trim().replaceAll("\\s+", " ");
        return limpio.isEmpty() ? null : limpio;
    }

    public static String truncar(String valor, int max) {
        return valor == null || valor.length() <= max ? valor : valor.substring(0, max);
    }

    /**
     * Separa "Nombre y apellido completo" en {nombres, apellido} tomando la última palabra como apellido.
     * El orden real no se puede deducir: el administrador lo corrige a mano si hace falta.
     */
    public static String[] separarNombreApellido(String nombreCompleto) {
        String limpio = limpiar(nombreCompleto);
        if (limpio == null) {
            return new String[]{"", ""};
        }
        int ultimoEspacio = limpio.lastIndexOf(' ');
        if (ultimoEspacio < 0) {
            return new String[]{limpio, ""};
        }
        return new String[]{limpio.substring(0, ultimoEspacio), limpio.substring(ultimoEspacio + 1)};
    }

    public static LocalDate parsearFecha(String fecha) {
        String limpio = limpiar(fecha);
        if (limpio == null) {
            return null;
        }
        for (DateTimeFormatter formato : FORMATOS_FECHA) {
            try {
                return LocalDate.parse(limpio, formato);
            } catch (DateTimeParseException ignored) {
                // probar el siguiente formato
            }
        }
        return null;
    }

    public static LocalDateTime parsearFechaHora(String fechaHora) {
        String limpio = limpiar(fechaHora);
        if (limpio == null) {
            return null;
        }
        for (DateTimeFormatter formato : FORMATOS_FECHA_HORA) {
            try {
                return LocalDateTime.parse(limpio, formato);
            } catch (DateTimeParseException ignored) {
                // probar el siguiente formato
            }
        }
        LocalDate soloFecha = parsearFecha(limpio);
        return soloFecha != null ? soloFecha.atStartOfDay() : null;
    }

    /** "Sí", "si", "SI" -> true; "No" -> false; vacío o cualquier otra cosa -> null. */
    public static Boolean parsearSiNo(String valor) {
        String limpio = limpiar(valor);
        if (limpio == null) {
            return null;
        }
        String sinAcentos = Normalizer.normalize(limpio, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase();
        if (sinAcentos.startsWith("si")) {
            return true;
        }
        if (sinAcentos.startsWith("no")) {
            return false;
        }
        return null;
    }
}
