package com.Capinteria.carpinteria.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Habilita @Async con el executor de Spring Boot (applicationTaskExecutor).
 * Se usa para mandar el aviso de consulta nueva sin demorar la respuesta al cliente.
 * No usar CompletableFuture.runAsync para eso: corre en el ForkJoinPool común, cuyos hilos
 * no tienen el classloader de la app cuando corre como jar, y Jakarta Mail falla
 * ("No provider of jakarta.mail.util.StreamProvider was found").
 */
@Configuration
@EnableAsync
public class AsyncConfig {
}
