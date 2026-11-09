package com.Capinteria.carpinteria.flows;

import com.Capinteria.carpinteria.Entity.Categoria;
import com.Capinteria.carpinteria.support.IntegrationTestBase;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/** Alta, edición, baja lógica y todas las vistas del catálogo de muebles. */
// El análisis de nulos de Eclipse marca los valores sin anotar que se pasan a APIs de Spring
// anotadas @NonNull (MediaType, String, ids...). Nunca son null acá; mismo criterio que el resto del proyecto.
@SuppressWarnings("null")
class MuebleFlowTest extends IntegrationTestBase {

    private MvcResult bajaLogica(long id) throws Exception {
        return mockMvc.perform(delete("/api/v1/mueble/baja-logica/" + id)
                .header(HttpHeaders.AUTHORIZATION, adminToken())).andReturn();
    }

    private List<String> nombres(JsonNode lista) {
        List<String> nombres = new ArrayList<>();
        lista.forEach(m -> nombres.add(m.path("nombreMueble").asText()));
        return nombres;
    }

    // ---------- Alta ----------

    @Test
    void crearMuebleGuardaSusImagenesAsociadasYLaPortadaElegida() throws Exception {
        Categoria cat = crearCategoria("Mesas");

        long id = crearMueble("Mesa ratona", "Roble", cat.getId(), 3, 1, false);

        Integer imagenesDelMueble = jdbc.queryForObject(
                "SELECT COUNT(*) FROM mueble_imagenes WHERE mueble_id = ?", Integer.class, id);
        assertThat(imagenesDelMueble).as("imágenes con mueble_id del mueble creado").isEqualTo(3);

        JsonNode imagenes = getJson("/api/v1/mueble/" + id + "/imagenes");
        assertThat(imagenes).hasSize(3);
        long portadas = 0;
        for (JsonNode img : imagenes) if (img.path("esPortada").asBoolean()) portadas++;
        assertThat(portadas).isEqualTo(1);
    }

    @Test
    void crearMuebleSinImagenesEsRechazado() throws Exception {
        Categoria cat = crearCategoria("Mesas");

        MvcResult result = mockMvc.perform(multipart("/api/v1/mueble/create")
                .param("mueble", toJson(Map.of("nombreMueble", "Sin fotos")))
                .param("categoriaId", String.valueOf(cat.getId()))
                .header(HttpHeaders.AUTHORIZATION, adminToken())).andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(400);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM mueble", Integer.class)).isZero();
    }

    @Test
    void crearMuebleEnCategoriaInexistenteEsRechazado() throws Exception {
        MvcResult result = mockMvc.perform(multipart("/api/v1/mueble/create")
                .file(imagen("a"))
                .param("mueble", toJson(Map.of("nombreMueble", "Huérfano")))
                .param("categoriaId", "999999")
                .header(HttpHeaders.AUTHORIZATION, adminToken())).andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(400);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM mueble", Integer.class)).isZero();
    }

    @Test
    void crearMuebleEnCategoriaDadaDeBajaEsRechazado() throws Exception {
        Categoria baja = crearCategoriaDadaDeBaja("Vieja");

        MvcResult result = mockMvc.perform(multipart("/api/v1/mueble/create")
                .file(imagen("a"))
                .param("mueble", toJson(Map.of("nombreMueble", "En baja")))
                .param("categoriaId", String.valueOf(baja.getId()))
                .header(HttpHeaders.AUTHORIZATION, adminToken())).andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(400);
    }

    // ---------- Lecturas del catálogo ----------

    @Test
    void getOneDevuelveElMueble() throws Exception {
        Categoria cat = crearCategoria("Camas");
        long id = crearMueble("Cama box", cat.getId());

        JsonNode mueble = getJson("/api/v1/mueble/" + id);

        assertThat(mueble.path("nombreMueble").asText()).isEqualTo("Cama box");
        assertThat(mueble.path("categoria").path("nombreCategoria").asText()).isEqualTo("Camas");
    }

    @Test
    void catalogoCompletoListaSoloActivosConSuPortada() throws Exception {
        Categoria cat = crearCategoria("Racks");
        crearMueble("Rack TV", cat.getId());
        long baja = crearMueble("Rack viejo", cat.getId());
        bajaLogica(baja);

        JsonNode page = getJson("/api/v1/mueble/catalogo-completo/0");

        assertThat(nombres(page.path("content"))).containsExactly("Rack TV");
        assertThat(page.path("totalElements").asLong()).isEqualTo(1);
        assertThat(page.path("content").get(0).path("imagenPortada").asText()).isNotBlank();
    }

    @Test
    void obtenerTodosLosMueblesUsaLaConsultaNativaYExcluyeBajas() throws Exception {
        Categoria cat = crearCategoria("Racks");
        crearMueble("B rack", cat.getId());
        crearMueble("A rack", cat.getId());
        long baja = crearMueble("C rack", cat.getId());
        bajaLogica(baja);

        JsonNode page = getJson("/api/v1/mueble/obtener-todos-muebles/0");

        assertThat(nombres(page.path("content"))).containsExactly("A rack", "B rack");
        assertThat(page.path("content").get(0).path("nombreCategoria").asText()).isEqualTo("Racks");
        assertThat(page.path("content").get(0).path("imagenPortada").asText()).isNotBlank();
    }

    @Test
    void catalogoPorCategoriaFiltraYExcluyeBajas() throws Exception {
        Categoria cocinas = crearCategoria("Cocinas");
        Categoria camas = crearCategoria("Camas");
        crearMueble("Alacena", cocinas.getId());
        crearMueble("Cama simple", camas.getId());
        long baja = crearMueble("Bajo mesada", cocinas.getId());
        bajaLogica(baja);

        JsonNode page = getJson("/api/v1/mueble/muebles-por-categoria/0/categoria/" + cocinas.getId());

        assertThat(nombres(page.path("content"))).containsExactly("Alacena");
        assertThat(page.path("categoriaNombre").asText()).isEqualTo("Cocinas");
    }

    @Test
    void catalogoPorCategoriaInexistenteDevuelve404() throws Exception {
        int status = mockMvc.perform(get("/api/v1/mueble/muebles-por-categoria/0/categoria/999999"))
                .andReturn().getResponse().getStatus();

        assertThat(status).isEqualTo(404);
    }

    @Test
    void catalogoPorCategoriaDadaDeBajaDevuelve400() throws Exception {
        Categoria baja = crearCategoriaDadaDeBaja("Vieja");

        int status = mockMvc.perform(get("/api/v1/mueble/muebles-por-categoria/0/categoria/" + baja.getId()))
                .andReturn().getResponse().getStatus();

        assertThat(status).isEqualTo(400);
    }

    @Test
    void busquedaPorNombreOColorIgnoraMayusculasYExcluyeBajas() throws Exception {
        Categoria cat = crearCategoria("Mesas");
        crearMueble("Mesa comedor", "Nogal", cat.getId(), 1, 0, false);
        crearMueble("Silla", "Blanco", cat.getId(), 1, 0, false);
        crearMueble("Banco", "NOGAL oscuro", cat.getId(), 1, 0, false);
        long baja = crearMueble("Mesa vieja", "Pino", cat.getId(), 1, 0, false);
        bajaLogica(baja);

        assertThat(nombres(getJson("/api/v1/mueble/filtrar/buscar/MESA/0").path("content")))
                .containsExactly("Mesa comedor");
        assertThat(nombres(getJson("/api/v1/mueble/filtrar/buscar/nogal/0").path("content")))
                .containsExactlyInAnyOrder("Mesa comedor", "Banco");
        assertThat(getJson("/api/v1/mueble/filtrar/buscar/inexistente/0").path("totalElements").asLong()).isZero();
    }

    @Test
    void destacadosDevuelveUnMueblePorCategoria() throws Exception {
        Categoria a = crearCategoria("Camas");
        Categoria b = crearCategoria("Mesas");
        crearMueble("Cama 1", a.getId());
        crearMueble("Cama 2", a.getId());
        crearMueble("Mesa 1", b.getId());

        JsonNode destacados = getJson("/api/v1/mueble/destacados");

        assertThat(nombres(destacados)).containsExactlyInAnyOrder("Cama 1", "Mesa 1");
    }

    @Test
    void novedadesMuestraSoloLosMarcadosParaPublicar() throws Exception {
        Categoria cat = crearCategoria("Mesas");
        crearMueble("Publicado", "Roble", cat.getId(), 1, 0, true);
        crearMueble("No publicado", "Roble", cat.getId(), 1, 0, false);

        JsonNode page = getJson("/api/v1/mueble/novedades/0");

        assertThat(nombres(page.path("content"))).containsExactly("Publicado");
    }

    // ---------- Edición y baja ----------

    @Test
    void modificarActualizaLosDatosYLosLinksDeRedes() throws Exception {
        Categoria cat = crearCategoria("Mesas");
        long id = crearMueble("Mesa", cat.getId());

        MvcResult result = mockMvc.perform(put("/api/v1/mueble/modificar/" + id)
                .header(HttpHeaders.AUTHORIZATION, adminToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of(
                        "nombreMueble", "Mesa extensible",
                        "colorMueble", "Wengue",
                        "descripcion", "Nueva descripción",
                        "linkInstagram", "https://instagram.com/p/abc",
                        "linkFacebook", "https://facebook.com/p/abc")))).andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        JsonNode mueble = getJson("/api/v1/mueble/" + id);
        assertThat(mueble.path("nombreMueble").asText()).isEqualTo("Mesa extensible");
        assertThat(mueble.path("colorMueble").asText()).isEqualTo("Wengue");
        assertThat(mueble.path("linkInstagram").asText()).isEqualTo("https://instagram.com/p/abc");
        assertThat(mueble.path("linkFacebook").asText()).isEqualTo("https://facebook.com/p/abc");
        assertThat(mueble.path("fechaModificacionMueble").asText()).isNotBlank();
        // La edición no toca las imágenes
        assertThat(getJson("/api/v1/mueble/" + id + "/imagenes")).hasSize(1);
    }

    @Test
    void modificarUnMuebleInexistenteDevuelve404() throws Exception {
        int status = mockMvc.perform(put("/api/v1/mueble/modificar/999999")
                .header(HttpHeaders.AUTHORIZATION, adminToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombreMueble\":\"X\"}")).andReturn().getResponse().getStatus();

        assertThat(status).isEqualTo(404);
    }

    @Test
    void bajaLogicaLoPasaAlListadoDeDadosDeBaja() throws Exception {
        Categoria cat = crearCategoria("Mesas");
        long id = crearMueble("Mesa a dar de baja", cat.getId());

        assertThat(bajaLogica(id).getResponse().getStatus()).isEqualTo(200);

        JsonNode bajas = getJson("/api/v1/mueble/dados-de-baja/0");
        assertThat(nombres(bajas.path("muebles"))).containsExactly("Mesa a dar de baja");
        // Y ya no se puede editar ni ver sus imágenes
        int statusModificar = mockMvc.perform(put("/api/v1/mueble/modificar/" + id)
                .header(HttpHeaders.AUTHORIZATION, adminToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombreMueble\":\"X\"}")).andReturn().getResponse().getStatus();
        assertThat(statusModificar).isEqualTo(404);
        int statusImagenes = mockMvc.perform(get("/api/v1/mueble/" + id + "/imagenes"))
                .andReturn().getResponse().getStatus();
        assertThat(statusImagenes).isEqualTo(400);
    }

    @Test
    void noSePuedeDarDeBajaDosVeces() throws Exception {
        Categoria cat = crearCategoria("Mesas");
        long id = crearMueble("Mesa", cat.getId());
        bajaLogica(id);

        assertThat(bajaLogica(id).getResponse().getStatus()).isEqualTo(400);
    }

    @Test
    void imagenesDeUnMuebleInexistenteDevuelve404() throws Exception {
        int status = mockMvc.perform(get("/api/v1/mueble/999999/imagenes")).andReturn().getResponse().getStatus();

        assertThat(status).isEqualTo(404);
    }
}
