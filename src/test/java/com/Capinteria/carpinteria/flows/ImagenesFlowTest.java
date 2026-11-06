package com.Capinteria.carpinteria.flows;

import com.Capinteria.carpinteria.Entity.Categoria;
import com.Capinteria.carpinteria.support.IntegrationTestBase;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/** Gestión de la galería de un mueble: agregar, eliminar y cambiar la portada. */
class ImagenesFlowTest extends IntegrationTestBase {

    private JsonNode imagenes(long muebleId) throws Exception {
        return getJson("/api/v1/mueble/" + muebleId + "/imagenes");
    }

    private List<Long> idsPortada(long muebleId) throws Exception {
        List<Long> ids = new ArrayList<>();
        for (JsonNode img : imagenes(muebleId)) {
            if (img.path("esPortada").asBoolean()) ids.add(img.path("id").asLong());
        }
        return ids;
    }

    private long idImagenNoPortada(long muebleId) throws Exception {
        for (JsonNode img : imagenes(muebleId)) {
            if (!img.path("esPortada").asBoolean()) return img.path("id").asLong();
        }
        throw new AssertionError("El mueble no tiene imágenes que no sean portada");
    }

    private MvcResult agregar(long muebleId, int cantidad, Integer portadaIndex) throws Exception {
        var req = multipart("/api/v1/mueble/" + muebleId + "/agregar-imagenes");
        for (int i = 0; i < cantidad; i++) req.file(imagen("extra" + i));
        if (portadaIndex != null) req.param("portadaIndex", String.valueOf(portadaIndex));
        return mockMvc.perform(req.header(HttpHeaders.AUTHORIZATION, adminToken())).andReturn();
    }

    @Test
    void agregarImagenesLasSumaALaGaleria() throws Exception {
        Categoria cat = crearCategoria("Mesas");
        long id = crearMueble("Mesa", "Roble", cat.getId(), 1, 0, false);

        MvcResult result = agregar(id, 2, null);

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        assertThat(imagenes(id)).hasSize(3);
        assertThat(idsPortada(id)).hasSize(1);
    }

    @Test
    void agregarImagenesConNuevaPortadaDejaUnaSolaPortada() throws Exception {
        Categoria cat = crearCategoria("Mesas");
        long id = crearMueble("Mesa", "Roble", cat.getId(), 2, 0, false);

        agregar(id, 2, 1);

        assertThat(imagenes(id)).hasSize(4);
        assertThat(idsPortada(id)).hasSize(1);
    }

    @Test
    void agregarConIndiceDePortadaInvalidoEsRechazado() throws Exception {
        Categoria cat = crearCategoria("Mesas");
        long id = crearMueble("Mesa", "Roble", cat.getId(), 1, 0, false);

        assertThat(agregar(id, 1, 5).getResponse().getStatus()).isEqualTo(400);
        assertThat(imagenes(id)).hasSize(1);
    }

    @Test
    void agregarAMuebleInexistenteDevuelve404() throws Exception {
        assertThat(agregar(999999, 1, null).getResponse().getStatus()).isEqualTo(404);
    }

    @Test
    void establecerPortadaDejaUnaSolaPortada() throws Exception {
        Categoria cat = crearCategoria("Mesas");
        long id = crearMueble("Mesa", "Roble", cat.getId(), 3, 0, false);
        long nueva = idImagenNoPortada(id);

        MvcResult result = mockMvc.perform(put("/api/v1/mueble/imagen/" + nueva + "/establecer-portada")
                .header(HttpHeaders.AUTHORIZATION, adminToken())).andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        assertThat(idsPortada(id)).containsExactly(nueva);
    }

    @Test
    void establecerPortadaDeImagenInexistenteDevuelve404() throws Exception {
        int status = mockMvc.perform(put("/api/v1/mueble/imagen/999999/establecer-portada")
                .header(HttpHeaders.AUTHORIZATION, adminToken())).andReturn().getResponse().getStatus();

        assertThat(status).isEqualTo(404);
    }

    @Test
    void eliminarUnaImagenComunLaSacaDeLaGaleria() throws Exception {
        Categoria cat = crearCategoria("Mesas");
        long id = crearMueble("Mesa", "Roble", cat.getId(), 3, 0, false);
        long aBorrar = idImagenNoPortada(id);

        MvcResult result = mockMvc.perform(delete("/api/v1/mueble/imagen/" + aBorrar)
                .header(HttpHeaders.AUTHORIZATION, adminToken())).andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        assertThat(imagenes(id)).hasSize(2);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM mueble_imagenes WHERE id = ?", Integer.class, aBorrar)).isZero();
    }

    @Test
    void eliminarLaPortadaAsignaOtraComoPortada() throws Exception {
        Categoria cat = crearCategoria("Mesas");
        long id = crearMueble("Mesa", "Roble", cat.getId(), 2, 0, false);
        long portada = idsPortada(id).get(0);

        MvcResult result = mockMvc.perform(delete("/api/v1/mueble/imagen/" + portada)
                .header(HttpHeaders.AUTHORIZATION, adminToken())).andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        assertThat(imagenes(id)).hasSize(1);
        assertThat(idsPortada(id)).hasSize(1).doesNotContain(portada);
    }

    @Test
    void eliminarImagenInexistenteDevuelve404() throws Exception {
        int status = mockMvc.perform(delete("/api/v1/mueble/imagen/999999")
                .header(HttpHeaders.AUTHORIZATION, adminToken())).andReturn().getResponse().getStatus();

        assertThat(status).isEqualTo(404);
    }
}
