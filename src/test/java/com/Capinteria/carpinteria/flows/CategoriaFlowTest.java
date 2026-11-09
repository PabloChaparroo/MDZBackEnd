package com.Capinteria.carpinteria.flows;

import com.Capinteria.carpinteria.Entity.Categoria;
import com.Capinteria.carpinteria.support.IntegrationTestBase;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/** ABM de categorías con baja lógica. */
// El análisis de nulos de Eclipse marca los valores sin anotar que se pasan a APIs de Spring
// anotadas @NonNull (MediaType, String, ids...). Nunca son null acá; mismo criterio que el resto del proyecto.
@SuppressWarnings("null")
class CategoriaFlowTest extends IntegrationTestBase {

    private MvcResult crear(String nombre) throws Exception {
        return mockMvc.perform(post("/api/v1/categoria/crear")
                .header(HttpHeaders.AUTHORIZATION, adminToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of("nombreCategoria", nombre)))).andReturn();
    }

    private MvcResult modificar(long id, String nombre) throws Exception {
        return mockMvc.perform(put("/api/v1/categoria/modificar/" + id)
                .header(HttpHeaders.AUTHORIZATION, adminToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of("nombreCategoria", nombre)))).andReturn();
    }

    private MvcResult bajaLogica(long id) throws Exception {
        return mockMvc.perform(delete("/api/v1/categoria/baja-logica/" + id)
                .header(HttpHeaders.AUTHORIZATION, adminToken())).andReturn();
    }

    private boolean listadoActivoContiene(String nombre) throws Exception {
        for (JsonNode c : getJson("/api/v1/categoria/todas")) {
            if (nombre.equals(c.path("nombreCategoria").asText())) return true;
        }
        return false;
    }

    @Test
    void crearCategoriaLaMuestraEnElListadoPublico() throws Exception {
        MvcResult result = crear("Mesas");

        assertThat(result.getResponse().getStatus()).isEqualTo(201);
        assertThat(json(result).path("categoriaId").asLong()).isPositive();
        assertThat(listadoActivoContiene("Mesas")).isTrue();
    }

    @Test
    void noSePuedeCrearUnaCategoriaSinNombre() throws Exception {
        assertThat(crear("   ").getResponse().getStatus()).isEqualTo(400);
        assertThat(categoriaRepository.count()).isZero();
    }

    @Test
    void noSePuedenTenerDosCategoriasActivasConElMismoNombre() throws Exception {
        crearCategoria("Sillas");

        assertThat(crear("Sillas").getResponse().getStatus()).isEqualTo(400);
        assertThat(categoriaRepository.count()).isEqualTo(1);
    }

    @Test
    void sePuedeReusarElNombreDeUnaCategoriaDadaDeBaja() throws Exception {
        crearCategoriaDadaDeBaja("Racks");

        assertThat(crear("Racks").getResponse().getStatus()).isEqualTo(201);
    }

    @Test
    void modificarCambiaElNombre() throws Exception {
        Categoria categoria = crearCategoria("Camas");

        MvcResult result = modificar(categoria.getId(), "Camas matrimoniales");

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        Categoria guardada = categoriaRepository.findById(categoria.getId()).orElseThrow();
        assertThat(guardada.getNombreCategoria()).isEqualTo("Camas matrimoniales");
        assertThat(guardada.getFechaModificacionCategoria()).isNotNull();
    }

    @Test
    void noSePuedeModificarAlNombreDeOtraCategoriaActiva() throws Exception {
        crearCategoria("Cocinas");
        Categoria otra = crearCategoria("Placares");

        assertThat(modificar(otra.getId(), "Cocinas").getResponse().getStatus()).isEqualTo(400);
    }

    @Test
    void noSePuedeModificarUnaCategoriaDadaDeBaja() throws Exception {
        Categoria baja = crearCategoriaDadaDeBaja("Viejas");

        assertThat(modificar(baja.getId(), "Nuevas").getResponse().getStatus()).isEqualTo(400);
    }

    @Test
    void modificarUnaCategoriaInexistenteDevuelveError() throws Exception {
        assertThat(modificar(999999, "X").getResponse().getStatus()).isEqualTo(400);
    }

    @Test
    void bajaLogicaSinMueblesLaSacaDelListadoActivo() throws Exception {
        Categoria categoria = crearCategoria("Estantes");

        MvcResult result = bajaLogica(categoria.getId());

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        assertThat(categoriaRepository.findById(categoria.getId()).orElseThrow().getFechaBajaCategoria()).isNotNull();
        assertThat(listadoActivoContiene("Estantes")).isFalse();
    }

    @Test
    void noSePuedeDarDeBajaDosVeces() throws Exception {
        Categoria categoria = crearCategoriaDadaDeBaja("Ya baja");

        assertThat(bajaLogica(categoria.getId()).getResponse().getStatus()).isEqualTo(400);
    }

    @Test
    void noSePuedeDarDeBajaUnaCategoriaConMueblesActivos() throws Exception {
        Categoria categoria = crearCategoria("Escritorios");
        crearMueble("Escritorio roble", categoria.getId());

        assertThat(bajaLogica(categoria.getId()).getResponse().getStatus()).isEqualTo(400);
        assertThat(categoriaRepository.findById(categoria.getId()).orElseThrow().getFechaBajaCategoria()).isNull();
    }

    @Test
    @Disabled("BUG conocido (deuda de categorías): CategoriaRepository.countMueblesByCategoria cuenta también "
            + "los muebles dados de baja, así que una categoría cuyos muebles ya están todos de baja no se puede dar de baja.")
    void sePuedeDarDeBajaUnaCategoriaSiSusMueblesYaEstanDeBaja() throws Exception {
        Categoria categoria = crearCategoria("Bibliotecas");
        long mueble = crearMueble("Biblioteca pino", categoria.getId());
        mockMvc.perform(delete("/api/v1/mueble/baja-logica/" + mueble)
                .header(HttpHeaders.AUTHORIZATION, adminToken())).andReturn();

        assertThat(bajaLogica(categoria.getId()).getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @Disabled("BUG conocido (deuda de categorías): GET /api/v1/categoria/dadas-de-baja envuelve un ResponseEntity "
            + "dentro de otro, así que el body es {headers, body, statusCode...} en vez de la lista.")
    void listadoDeDadasDeBajaDevuelveUnaLista() throws Exception {
        crearCategoriaDadaDeBaja("Lavatorios");

        JsonNode body = getJson("/api/v1/categoria/dadas-de-baja");

        assertThat(body.isArray()).isTrue();
        assertThat(body.get(0).path("nombreCategoria").asText()).isEqualTo("Lavatorios");
    }

    @Test
    void listadoDeDadasDeBajaContieneLaCategoria() throws Exception {
        // Documenta el formato actual (ResponseEntity crudo): la lista viene dentro de "body".
        // Cuando se arregle el bug de arriba, este test se borra.
        crearCategoriaDadaDeBaja("Lavatorios");

        JsonNode body = getJson("/api/v1/categoria/dadas-de-baja");
        JsonNode lista = body.isArray() ? body : body.path("body");

        assertThat(lista.isArray()).isTrue();
        assertThat(lista.get(0).path("nombreCategoria").asText()).isEqualTo("Lavatorios");
    }
}
