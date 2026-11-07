package com.Capinteria.carpinteria.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint público para el health check de Render y el keep-alive externo.
 * No toca la base a propósito: así Neon puede suspenderse y no gasta horas de cómputo.
 */
@RestController
public class PingController {

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }
}
