package com.Capinteria.carpinteria.Seed;

import com.Capinteria.carpinteria.Entity.MuebleImagenes;
import com.Capinteria.carpinteria.Repositories.MuebleImagenesRepository;
import com.Capinteria.carpinteria.Util.Miniaturas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Genera la miniatura de las fotos que todavia no la tienen (las cargadas antes de existir la columna).
 * Corre en segundo plano y de a una foto, para no demorar el arranque ni cargar la memoria.
 */
@Component
public class MiniaturaBackfill implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(MiniaturaBackfill.class);
    private final MuebleImagenesRepository repository;

    public MiniaturaBackfill(MuebleImagenesRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(ApplicationArguments args) {
        Thread hilo = new Thread(this::completarFaltantes, "miniatura-backfill");
        hilo.setDaemon(true);
        hilo.start();
    }

    private void completarFaltantes() {
        try {
            List<Long> ids = repository.idsSinMiniatura();
            if (ids.isEmpty()) {
                return;
            }
            logger.info("[MINIATURAS] Generando miniaturas para {} imagenes existentes", ids.size());
            int generadas = 0;
            for (Long id : ids) {
                MuebleImagenes imagen = repository.findById(id).orElse(null);
                if (imagen == null) {
                    continue;
                }
                byte[] miniatura = Miniaturas.generar(imagen.getImagenes());
                if (miniatura != null) {
                    repository.guardarMiniatura(id, miniatura);
                    generadas++;
                }
            }
            logger.info("[MINIATURAS] Listo: {} de {} miniaturas generadas", generadas, ids.size());
        } catch (Exception e) {
            logger.error("[MINIATURAS] No se pudo completar el relleno de miniaturas: {}", e.getMessage());
        }
    }
}
