package com.Capinteria.carpinteria.Seed;

import com.Capinteria.carpinteria.Entity.Categoria;
import com.Capinteria.carpinteria.Entity.Mueble;
import com.Capinteria.carpinteria.Entity.MuebleImagenes;
import com.Capinteria.carpinteria.Repositories.CategoriaRepository;
import com.Capinteria.carpinteria.Repositories.MuebleImagenesRepository;
import com.Capinteria.carpinteria.Repositories.MuebleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Carga categorías y muebles con fotos reales para probar el catálogo/admin en
 * desarrollo. Los datos salen de resources/seed/muebles (catalogo.csv y un JPG
 * por mueble; los créditos de las fotos están en CREDITOS.md). Solo se activa
 * con el perfil "seed" y saltea los muebles que ya existen en su categoría,
 * para no duplicar datos en corridas repetidas.
 *
 * Uso: mvnw spring-boot:run "-Dspring-boot.run.profiles=local,seed"
 */
@Component
@Profile("seed")
public class DataSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);
    private static final String RUTA_SEED = "seed/muebles/";

    private final CategoriaRepository categoriaRepository;
    private final MuebleRepository muebleRepository;
    private final MuebleImagenesRepository muebleImagenesRepository;

    public DataSeeder(CategoriaRepository categoriaRepository, MuebleRepository muebleRepository,
                       MuebleImagenesRepository muebleImagenesRepository) {
        this.categoriaRepository = categoriaRepository;
        this.muebleRepository = muebleRepository;
        this.muebleImagenesRepository = muebleImagenesRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("[SEED] Iniciando carga de muebles con fotos reales...");
        int totalCreados = 0;
        String fechaAlta = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        Set<String> existentes = new HashSet<>();
        for (Mueble mueble : muebleRepository.findAll()) {
            if (mueble.getCategoria() != null) {
                existentes.add(clave(mueble.getCategoria().getNombreCategoria(), mueble.getNombreMueble()));
            }
        }

        try (InputStream is = new ClassPathResource(RUTA_SEED + "catalogo.csv").getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            reader.readLine(); // encabezado
            String linea;
            while ((linea = reader.readLine()) != null) {
                String[] campos = linea.split(";", 5);
                String nombreCategoria = campos[0];
                String nombre = campos[2];
                if (!existentes.add(clave(nombreCategoria, nombre))) {
                    continue;
                }

                Mueble mueble = new Mueble();
                mueble.setNombreMueble(nombre);
                mueble.setColorMueble(campos[3]);
                mueble.setDescripcion(campos[4]);
                mueble.setFechaAltaMueble(fechaAlta);
                mueble.setCategoria(buscarOCrearCategoria(nombreCategoria));
                mueble.setImagenes(new ArrayList<>());

                Mueble savedMueble = muebleRepository.save(mueble);

                MuebleImagenes imagen = new MuebleImagenes();
                imagen.setEsPortada(true);
                imagen.setImagenes(leerImagen(campos[1]));
                imagen.setMiniatura(com.Capinteria.carpinteria.Util.Miniaturas.generar(imagen.getImagenes()));
                savedMueble.getImagenes().add(imagen);
                muebleImagenesRepository.save(imagen);
                muebleRepository.save(savedMueble);

                totalCreados++;
            }
        }

        logger.info("[SEED] Listo: {} muebles creados.", totalCreados);
    }

    private String clave(String categoria, String nombre) {
        return (categoria + "|" + nombre).toLowerCase();
    }

    private Categoria buscarOCrearCategoria(String nombre) {
        return categoriaRepository.findAll().stream()
            .filter(c -> c.getNombreCategoria().equalsIgnoreCase(nombre) && c.getFechaBajaCategoria() == null)
            .findFirst()
            .orElseGet(() -> {
                Categoria nueva = new Categoria();
                nueva.setNombreCategoria(nombre);
                nueva.setFechaAltaCategoria(new Date());
                return categoriaRepository.save(nueva);
            });
    }

    private byte[] leerImagen(String archivo) throws IOException {
        try (InputStream is = new ClassPathResource(RUTA_SEED + archivo).getInputStream()) {
            return is.readAllBytes();
        }
    }
}
