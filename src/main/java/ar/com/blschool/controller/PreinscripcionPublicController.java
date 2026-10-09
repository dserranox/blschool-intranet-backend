package ar.com.blschool.controller;

import ar.com.blschool.dto.PreinscripcionFormDTO;
import ar.com.blschool.dto.PreinscripcionResultadoDTO;
import ar.com.blschool.service.PreinscripcionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Endpoint llamado por el Apps Script del Google Form de inscripciones.
 * No usa JWT: se autentica con la clave compartida del header X-Api-Key
 * (propiedad preinscripcion.api-key / variable de entorno PREINSCRIPCION_API_KEY).
 */
@RestController
@RequestMapping("/public/preinscripciones")
public class PreinscripcionPublicController {

    private final PreinscripcionService preinscripcionService;
    private final String apiKey;

    public PreinscripcionPublicController(PreinscripcionService preinscripcionService,
                                          @Value("${preinscripcion.api-key:}") String apiKey) {
        this.preinscripcionService = preinscripcionService;
        this.apiKey = apiKey;
    }

    @PostMapping
    public PreinscripcionResultadoDTO recibir(@RequestHeader(value = "X-Api-Key", required = false) String apiKeyRecibida,
                                              @RequestBody PreinscripcionFormDTO form) {
        validarApiKey(apiKeyRecibida);
        return preinscripcionService.procesarFormulario(form);
    }

    private void validarApiKey(String recibida) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Preinscripción online no configurada");
        }
        if (recibida == null || !MessageDigest.isEqual(
                apiKey.getBytes(StandardCharsets.UTF_8), recibida.getBytes(StandardCharsets.UTF_8))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "API key inválida");
        }
    }
}
