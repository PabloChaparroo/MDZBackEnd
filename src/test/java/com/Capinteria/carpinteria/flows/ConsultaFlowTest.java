package com.Capinteria.carpinteria.flows;

import com.Capinteria.carpinteria.Entity.Categoria;
import com.Capinteria.carpinteria.support.IntegrationTestBase;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/** Formularios de consulta (Home y detalle de mueble) y respuesta por mail desde el panel. */
// El análisis de nulos de Eclipse marca los valores sin anotar que se pasan a APIs de Spring
// anotadas @NonNull (MediaType, String, ids...). Nunca son null acá; mismo criterio que el resto del proyecto.
@SuppressWarnings("null")
class ConsultaFlowTest extends IntegrationTestBase {

    private MvcResult crearConsulta(String nombre, String mail, Long muebleId, String mensaje) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("cliente", Map.of(
                "nombreCliente", nombre,
                "apellidoCliente", "Gómez",
                "telefonoCliente", "2615551234",
                "mailCliente", mail));
        body.put("mensajeConsulta", mensaje);
        if (muebleId != null) body.put("muebleId", muebleId);
        // Sin token: los formularios son públicos
        return mockMvc.perform(post("/api/v1/consultas/crear")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(body))).andReturn();
    }

    private MvcResult responder(long consultaId, String respuesta) throws Exception {
        return mockMvc.perform(put("/api/v1/consultas/" + consultaId + "/responder")
                .header(HttpHeaders.AUTHORIZATION, adminToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of("respuesta", respuesta)))).andReturn();
    }

    private JsonNode listarConsultas() throws Exception {
        return json(mockMvc.perform(get("/api/v1/consultas/obtener-consultas/0")
                .header(HttpHeaders.AUTHORIZATION, adminToken())).andReturn());
    }

    // ---------- Crear ----------

    @Test
    void consultaGeneralCreaClienteYAvisaAlAdminPorMail() throws Exception {
        MvcResult result = crearConsulta("Lucía", "lucia@mail.com", null, "Hacen muebles a medida?");

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        JsonNode body = json(result);
        assertThat(body.path("clienteNuevo").asBoolean()).isTrue();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM cliente WHERE mail_cliente = 'lucia@mail.com'", Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT mueble_id FROM consulta WHERE id = ?", Long.class, body.path("consultaId").asLong())).isNull();
        verify(emailService).notificarNuevaConsulta(eq("Lucía Gómez"), eq("lucia@mail.com"), anyString(),
                eq("Hacen muebles a medida?"), isNull());
    }

    @Test
    void consultaSobreUnMuebleQuedaAsociadaAlMueble() throws Exception {
        Categoria cat = crearCategoria("Mesas");
        long mueble = crearMueble("Mesa ratona", cat.getId());

        MvcResult result = crearConsulta("Juan", "juan@mail.com", mueble, "Viene en blanco?");

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        long consultaId = json(result).path("consultaId").asLong();
        assertThat(jdbc.queryForObject("SELECT mueble_id FROM consulta WHERE id = ?", Long.class, consultaId)).isEqualTo(mueble);
        verify(emailService).notificarNuevaConsulta(anyString(), eq("juan@mail.com"), anyString(),
                eq("Viene en blanco?"), eq("Mesa ratona"));
    }

    @Test
    void segundaConsultaDelMismoMailReutilizaElCliente() throws Exception {
        crearConsulta("Lucía", "lucia@mail.com", null, "Primera");

        MvcResult segunda = crearConsulta("Lucía", "lucia@mail.com", null, "Segunda");

        assertThat(json(segunda).path("clienteNuevo").asBoolean()).isFalse();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM cliente", Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM consulta", Integer.class)).isEqualTo(2);
    }

    @Test
    void consultaSobreMuebleInexistenteEsRechazada() throws Exception {
        MvcResult result = crearConsulta("Juan", "juan@mail.com", 999999L, "Hola");

        assertThat(result.getResponse().getStatus()).isEqualTo(400);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM consulta", Integer.class)).isZero();
        verify(emailService, never()).notificarNuevaConsulta(any(), any(), any(), any(), any());
    }

    @Test
    void consultaSinDatosDelClienteEsRechazada() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/consultas/crear")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"mensajeConsulta\":\"Hola\"}")).andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(400);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM consulta", Integer.class)).isZero();
    }

    // ---------- Panel ----------

    @Test
    void elPanelListaLasConsultasDeLaMasNuevaALaMasVieja() throws Exception {
        crearConsulta("Primera", "a@mail.com", null, "Mensaje 1");
        Thread.sleep(10);
        crearConsulta("Segunda", "b@mail.com", null, "Mensaje 2");

        JsonNode page = listarConsultas();

        assertThat(page.path("totalElements").asLong()).isEqualTo(2);
        assertThat(page.path("content").get(0).path("mensajeConsulta").asText()).isEqualTo("Mensaje 2");
        assertThat(page.path("content").get(0).path("cliente").path("nombreCliente").asText()).isEqualTo("Segunda");
    }

    @Test
    void elPanelListaConsultasSobreMueblesConFotos() throws Exception {
        // La consulta trae el mueble (y su galería) para el modal "Ver mueble": no tiene que romper la serialización.
        Categoria cat = crearCategoria("Mesas");
        long mueble = crearMueble("Mesa", "Roble", cat.getId(), 2, 0, false);
        crearConsulta("Juan", "juan@mail.com", mueble, "Precio?");

        MvcResult result = mockMvc.perform(get("/api/v1/consultas/obtener-consultas/0")
                .header(HttpHeaders.AUTHORIZATION, adminToken())).andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        assertThat(json(result).path("content").get(0).path("mueble").path("nombreMueble").asText()).isEqualTo("Mesa");
    }

    @Test
    void filtrarPorNombreDelClienteIgnoraMayusculas() throws Exception {
        crearConsulta("Martina", "martina@mail.com", null, "Hola");
        crearConsulta("Pedro", "pedro@mail.com", null, "Hola");

        JsonNode page = json(mockMvc.perform(get("/api/v1/consultas/filtrar-por-nombre")
                .param("nombre", "MARTI")
                .header(HttpHeaders.AUTHORIZATION, adminToken())).andReturn());

        assertThat(page.path("totalElements").asLong()).isEqualTo(1);
        assertThat(page.path("content").get(0).path("cliente").path("nombreCliente").asText()).isEqualTo("Martina");
    }

    // ---------- Responder ----------

    @Test
    void responderMandaElMailYGuardaLaRespuesta() throws Exception {
        long id = json(crearConsulta("Lucía", "lucia@mail.com", null, "Hacen envíos?")).path("consultaId").asLong();

        MvcResult result = responder(id, "Sí, a todo Mendoza.");

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        verify(emailService).enviarRespuestaConsulta("lucia@mail.com", "Lucía", "Hacen envíos?", "Sí, a todo Mendoza.");
        assertThat(jdbc.queryForObject("SELECT respuesta FROM consulta WHERE id = ?", String.class, id))
                .isEqualTo("Sí, a todo Mendoza.");
    }

    @Test
    void siElMailFallaNoSeGuardaLaRespuesta() throws Exception {
        long id = json(crearConsulta("Lucía", "lucia@mail.com", null, "Hacen envíos?")).path("consultaId").asLong();
        doThrow(new RuntimeException("SMTP caído"))
                .when(emailService).enviarRespuestaConsulta(anyString(), anyString(), anyString(), anyString());

        MvcResult result = responder(id, "Sí.");

        assertThat(result.getResponse().getStatus()).isEqualTo(500);
        assertThat(jdbc.queryForObject("SELECT respuesta FROM consulta WHERE id = ?", String.class, id)).isNull();
    }

    @Test
    void respuestaVaciaEsRechazada() throws Exception {
        long id = json(crearConsulta("Lucía", "lucia@mail.com", null, "Hola")).path("consultaId").asLong();

        assertThat(responder(id, "   ").getResponse().getStatus()).isEqualTo(400);
        verify(emailService, never()).enviarRespuestaConsulta(any(), any(), any(), any());
    }

    @Test
    void responderUnaConsultaInexistenteDevuelve404() throws Exception {
        assertThat(responder(999999, "Hola").getResponse().getStatus()).isEqualTo(404);
    }
}
