package com.Capinteria.carpinteria.Controller;

import com.Capinteria.carpinteria.Entity.DestinatarioNotificacion;
import com.Capinteria.carpinteria.Service.EmailService;
import com.Capinteria.carpinteria.Service.NotificacionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Casillas que reciben el aviso de consulta nueva (solo ADMIN, ver SecurityConfig). */
@RestController
@RequestMapping("/api/v1/notificaciones")
public class NotificacionController {

    private final NotificacionService notificacionService;
    private final EmailService emailService;

    public NotificacionController(NotificacionService notificacionService, EmailService emailService) {
        this.notificacionService = notificacionService;
        this.emailService = emailService;
    }

    @GetMapping
    public Map<String, Object> listar() {
        return Map.of(
                "destinatarios", notificacionService.listar(),
                "usaRespaldo", notificacionService.usaRespaldo());
    }

    @PostMapping
    public ResponseEntity<DestinatarioNotificacion> agregar(@RequestBody Map<String, String> body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notificacionService.agregar(body.get("mail")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> quitar(@PathVariable Long id) {
        notificacionService.quitar(id);
        return ResponseEntity.noContent().build();
    }

    /** Manda un mail de prueba a esa casilla, en el momento, para confirmar que está bien escrita. */
    @PostMapping("/{id}/prueba")
    public ResponseEntity<Map<String, String>> probar(@PathVariable Long id) {
        DestinatarioNotificacion destinatario = notificacionService.buscar(id);
        try {
            emailService.enviarPruebaNotificacion(destinatario.getMail());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", "No se pudo enviar el mail de prueba. Revisá la configuración del correo."));
        }
        return ResponseEntity.ok(Map.of("mensaje", "Mail de prueba enviado a " + destinatario.getMail()));
    }
}
