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
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Carga categorías y muebles de prueba (con imagen de portada generada en el
 * momento) para tener datos reales con los que probar el catálogo/admin en
 * desarrollo. Solo se activa con el perfil "seed" y no hace nada si ya hay
 * 100 o más muebles cargados, para no duplicar datos en corridas repetidas.
 *
 * Uso: mvnw spring-boot:run "-Dspring-boot.run.profiles=local,seed"
 */
@Component
@Profile("seed")
public class DataSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    private final CategoriaRepository categoriaRepository;
    private final MuebleRepository muebleRepository;
    private final MuebleImagenesRepository muebleImagenesRepository;

    public DataSeeder(CategoriaRepository categoriaRepository, MuebleRepository muebleRepository,
                       MuebleImagenesRepository muebleImagenesRepository) {
        this.categoriaRepository = categoriaRepository;
        this.muebleRepository = muebleRepository;
        this.muebleImagenesRepository = muebleImagenesRepository;
    }

    private static final String[] COLORES = {
        "Roble natural", "Nogal", "Blanco", "Negro mate", "Gris",
        "Cedro", "Pino", "Wengué", "Haya", "Roble oscuro"
    };

    private record CategoriaSeed(String nombre, String colorHex, String[] productos) {}

    private static final List<CategoriaSeed> CATEGORIAS = List.of(
        new CategoriaSeed("Comedor", "8B5E3C", new String[]{
            "Mesa de comedor", "Mesa extensible", "Juego de sillas", "Aparador", "Vitrina",
            "Bufetera", "Mesa auxiliar", "Carro bar", "Banco comedor", "Mesa redonda",
            "Mesa rústica", "Alacena comedor", "Rincón comedor"
        }),
        new CategoriaSeed("Dormitorio", "6B4F4F", new String[]{
            "Cama matrimonial", "Cama individual", "Placar 3 puertas", "Placar 6 puertas", "Mesa de luz",
            "Cómoda", "Vestidor", "Cabecera tapizada", "Baulera", "Cama nido",
            "Ropero rústico", "Perchero de pie", "Banco pie de cama"
        }),
        new CategoriaSeed("Living", "4F6B5C", new String[]{
            "Rack para TV", "Biblioteca", "Mesa de centro", "Mesa ratona", "Estantería modular",
            "Mueble esquinero", "Panel para TV", "Consola", "Mesa lateral", "Organizador living",
            "Repisa flotante", "Mueble multiuso", "Mesa nido"
        }),
        new CategoriaSeed("Cocina", "B08968", new String[]{
            "Alacena superior", "Alacena inferior", "Isla de cocina", "Mesada de madera", "Despensero",
            "Carro de cocina", "Mueble bajo mesada", "Especiero", "Mueble esquinero cocina", "Barra desayunadora",
            "Estante de cocina", "Cajonera de cocina", "Mueble para horno"
        }),
        new CategoriaSeed("Oficina", "4A4A4A", new String[]{
            "Escritorio ejecutivo", "Escritorio compacto", "Biblioteca de oficina", "Archivador", "Mueble para PC",
            "Estación de trabajo", "Mesa de reuniones", "Cajonera de oficina", "Repisa de oficina", "Escritorio en L",
            "Mueble multimedia", "Mesa auxiliar de oficina", "Organizador de escritorio"
        }),
        new CategoriaSeed("Infantil", "5B8C85", new String[]{
            "Cuna funcional", "Cama infantil", "Escritorio infantil", "Juguetero", "Ropero infantil",
            "Cama cucheta", "Mesa de luz infantil", "Estante infantil", "Cambiador", "Biblioteca infantil",
            "Perchero infantil", "Baulera infantil", "Organizador infantil"
        }),
        new CategoriaSeed("Jardín", "6B8E4E", new String[]{
            "Mesa de jardín", "Reposera de madera", "Banco de jardín", "Soporte para sombrilla", "Deck modular",
            "Macetero de madera", "Pérgola", "Mesa plegable de jardín", "Silla de jardín", "Juego de jardín",
            "Mueble para exterior", "Estante de jardín", "Baúl de jardín"
        }),
        new CategoriaSeed("Baño", "4A6670", new String[]{
            "Mueble bajo mesada de baño", "Botiquín", "Repisa de baño", "Toallero de pie", "Mueble esquinero de baño",
            "Cesto para ropa sucia", "Espejo con marco de madera", "Banqueta de baño", "Organizador de baño", "Mueble para lavatorio",
            "Estante de baño", "Perchero de baño", "Mueble auxiliar de baño"
        })
    );

    @Override
    public void run(String... args) throws Exception {
        long totalExistente = muebleRepository.count();
        if (totalExistente >= 100) {
            logger.info("[SEED] Ya existen {} muebles en la base, se omite la carga de datos de prueba.", totalExistente);
            return;
        }

        logger.info("[SEED] Iniciando carga de categorías y muebles de prueba...");
        int totalCreados = 0;
        Random random = new Random(42);
        String fechaAlta = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        for (CategoriaSeed catSeed : CATEGORIAS) {
            Categoria categoria = buscarOCrearCategoria(catSeed.nombre());

            for (String producto : catSeed.productos()) {
                String color = COLORES[random.nextInt(COLORES.length)];

                Mueble mueble = new Mueble();
                mueble.setNombreMueble(producto);
                mueble.setColorMueble(color);
                mueble.setDescripcion(producto + " a medida, fabricado en madera maciza. Terminación "
                    + color.toLowerCase() + ", pensado para durar.");
                mueble.setFechaAltaMueble(fechaAlta);
                mueble.setCategoria(categoria);
                mueble.setImagenes(new ArrayList<>());

                Mueble savedMueble = muebleRepository.save(mueble);

                MuebleImagenes imagen = new MuebleImagenes();
                imagen.setEsPortada(true);
                imagen.setImagenes(generarImagenPlaceholder(producto, catSeed.colorHex()));
                savedMueble.getImagenes().add(imagen);
                muebleImagenesRepository.save(imagen);
                muebleRepository.save(savedMueble);

                totalCreados++;
            }
        }

        logger.info("[SEED] Listo: {} muebles creados en {} categorías.", totalCreados, CATEGORIAS.size());
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

    /** Genera una imagen placeholder simple (color sólido + nombre del producto) como PNG. */
    private byte[] generarImagenPlaceholder(String texto, String colorHex) throws IOException {
        int width = 640;
        int height = 480;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g.setColor(Color.decode("#" + colorHex));
        g.fillRect(0, 0, width, height);

        g.setColor(new Color(255, 255, 255, 230));
        g.setFont(new Font("SansSerif", Font.BOLD, 34));
        FontMetrics fm = g.getFontMetrics();
        List<String> lineas = partirTexto(texto, fm, width - 80);
        int lineHeight = fm.getHeight();
        int startY = height / 2 - (lineas.size() * lineHeight) / 2 + fm.getAscent();
        for (int i = 0; i < lineas.size(); i++) {
            String linea = lineas.get(i);
            int textWidth = fm.stringWidth(linea);
            g.drawString(linea, (width - textWidth) / 2, startY + i * lineHeight);
        }

        g.setFont(new Font("SansSerif", Font.PLAIN, 16));
        g.setColor(new Color(255, 255, 255, 160));
        String marca = "MDZ MUEBLES";
        FontMetrics fmSmall = g.getFontMetrics();
        g.drawString(marca, (width - fmSmall.stringWidth(marca)) / 2, height - 30);

        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return baos.toByteArray();
    }

    private List<String> partirTexto(String texto, FontMetrics fm, int maxWidth) {
        String[] palabras = texto.split(" ");
        List<String> lineas = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        for (String palabra : palabras) {
            String prueba = actual.isEmpty() ? palabra : actual + " " + palabra;
            if (fm.stringWidth(prueba) > maxWidth && !actual.isEmpty()) {
                lineas.add(actual.toString());
                actual = new StringBuilder(palabra);
            } else {
                actual = new StringBuilder(prueba);
            }
        }
        if (!actual.isEmpty()) {
            lineas.add(actual.toString());
        }
        return lineas;
    }
}
