package com.Capinteria.carpinteria.flows;

import com.Capinteria.carpinteria.Entity.Categoria;
import com.Capinteria.carpinteria.Util.Miniaturas;
import com.Capinteria.carpinteria.support.IntegrationTestBase;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Map;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

/** Los listados del catálogo devuelven la miniatura de la portada, no la foto original. */
@SuppressWarnings("null")
class MiniaturasFlowTest extends IntegrationTestBase {

    // Ruido aleatorio: un JPEG que no comprime bien, como una foto real de varios cientos de KB
    private byte[] fotoGrande() throws Exception {
        BufferedImage img = new BufferedImage(2000, 1500, BufferedImage.TYPE_INT_RGB);
        Random random = new Random(1);
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                img.setRGB(x, y, random.nextInt(0xFFFFFF));
            }
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(img, "jpg", out);
        return out.toByteArray();
    }

    @Test
    void miniaturaEsUnJpegChicoYMasLivianoQueElOriginal() throws Exception {
        byte[] original = fotoGrande();

        byte[] miniatura = Miniaturas.generar(original);

        assertThat(miniatura).isNotNull();
        assertThat(miniatura.length).isLessThan(original.length / 4);
        BufferedImage decodificada = ImageIO.read(new ByteArrayInputStream(miniatura));
        assertThat(Math.max(decodificada.getWidth(), decodificada.getHeight())).isEqualTo(640);
        // Mantiene la proporcion 4:3
        assertThat((double) decodificada.getWidth() / decodificada.getHeight()).isBetween(1.32, 1.34);
    }

    @Test
    void archivoQueNoEsImagenNoGeneraMiniatura() {
        assertThat(Miniaturas.generar("esto no es una foto".getBytes())).isNull();
        assertThat(Miniaturas.generar(null)).isNull();
    }

    @Test
    void elCatalogoDevuelveLaMiniaturaYElDetalleLaFotoOriginal() throws Exception {
        Categoria cat = crearCategoria("Mesas");
        byte[] original = fotoGrande();
        mockMvc.perform(multipart("/api/v1/mueble/create")
                .file(new MockMultipartFile("files", "grande.jpg", "image/jpeg", original))
                .param("mueble", toJson(Map.of("nombreMueble", "Mesa con foto real")))
                .param("categoriaId", String.valueOf(cat.getId()))
                .param("portadaIndex", "0")
                .header(HttpHeaders.AUTHORIZATION, adminToken())).andReturn();
        long id = jdbc.queryForObject("SELECT id FROM mueble WHERE nombre_mueble = 'Mesa con foto real'", Long.class);

        JsonNode catalogo = getJson("/api/v1/mueble/catalogo-completo/0").path("content");
        byte[] enCatalogo = Base64.getDecoder().decode(catalogo.get(0).path("imagenPortada").asText());
        assertThat(enCatalogo.length).as("el catálogo no manda la foto original").isLessThan(original.length / 4);

        JsonNode porCategoria = getJson("/api/v1/mueble/muebles-por-categoria/0/categoria/" + cat.getId()).path("content");
        byte[] enCategoria = Base64.getDecoder().decode(porCategoria.get(0).path("imagenPortada").path("imagenes").asText());
        assertThat(enCategoria.length).isLessThan(original.length / 4);

        JsonNode detalle = getJson("/api/v1/mueble/" + id + "/imagenes");
        byte[] enDetalle = Base64.getDecoder().decode(detalle.get(0).path("imagenes").asText());
        assertThat(enDetalle).as("la ficha del mueble sigue mostrando la foto original").isEqualTo(original);
        assertThat(detalle.get(0).has("miniatura")).as("la miniatura no se expone en la API").isFalse();
    }
}
