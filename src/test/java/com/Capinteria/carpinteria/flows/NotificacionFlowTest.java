package com.Capinteria.carpinteria.flows;

import com.Capinteria.carpinteria.Service.NotificacionService;
import com.Capinteria.carpinteria.support.IntegrationTestBase;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/** Casillas que reciben el aviso de consulta nueva, administradas por el admin desde Mi Perfil. */
class NotificacionFlowTest extends IntegrationTestBase {

    @Autowired
    private NotificacionService notificacionService;

    private MvcResult agregar(String mail) throws Exception {
        return mockMvc.perform(post("/api/v1/notificaciones")
                .header(HttpHeaders.AUTHORIZATION, adminToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of("mail", mail)))).andReturn();
    }

    private JsonNode listar() throws Exception {
        return json(mockMvc.perform(get("/api/v1/notificaciones")
                .header(HttpHeaders.AUTHORIZATION, adminToken())).andReturn());
    }

    @Test
    void sinCasillasCargadasSeUsaElRespaldo() throws Exception {
        JsonNode body = listar();

        assertThat(body.path("destinatarios")).isEmpty();
        assertThat(body.path("usaRespaldo").asBoolean()).isTrue();
    }

    @Test
    void agregarCasillasLasListaYDejaDeUsarElRespaldo() throws Exception {
        assertThat(agregar("hermano@gmail.com").getResponse().getStatus()).isEqualTo(201);
        assertThat(agregar("ventas@mdz.com").getResponse().getStatus()).isEqualTo(201);

        JsonNode body = listar();
        assertThat(body.path("usaRespaldo").asBoolean()).isFalse();
        assertThat(body.path("destinatarios")).hasSize(2);
        assertThat(body.path("destinatarios").get(0).path("mail").asText()).isEqualTo("hermano@gmail.com");
        assertThat(notificacionService.destinatariosDelAviso()).containsExactly("hermano@gmail.com", "ventas@mdz.com");
    }

    @Test
    void elMailSeGuardaSinEspaciosYEnMinusculas() throws Exception {
        agregar("  Hermano@Gmail.COM ");

        assertThat(listar().path("destinatarios").get(0).path("mail").asText()).isEqualTo("hermano@gmail.com");
    }

    @Test
    void noSeAceptanDuplicadosAunqueCambienLasMayusculas() throws Exception {
        agregar("hermano@gmail.com");

        assertThat(agregar("HERMANO@gmail.com").getResponse().getStatus()).isEqualTo(409);
        assertThat(listar().path("destinatarios")).hasSize(1);
    }

    @Test
    void mailsInvalidosSonRechazados() throws Exception {
        for (String invalido : new String[]{"", "   ", "sin-arroba", "a@b", "con espacio@gmail.com", "@gmail.com"}) {
            assertThat(agregar(invalido).getResponse().getStatus()).as("'%s'", invalido).isEqualTo(400);
        }
        assertThat(listar().path("destinatarios")).isEmpty();
    }

    @Test
    void quitarUnaCasillaLaSacaDeLaLista() throws Exception {
        long id = json(agregar("viejo@gmail.com")).path("id").asLong();
        agregar("nuevo@gmail.com");

        int status = mockMvc.perform(delete("/api/v1/notificaciones/" + id)
                .header(HttpHeaders.AUTHORIZATION, adminToken())).andReturn().getResponse().getStatus();

        assertThat(status).isEqualTo(204);
        assertThat(notificacionService.destinatariosDelAviso()).containsExactly("nuevo@gmail.com");
    }

    @Test
    void quitarTodasVuelveAlRespaldo() throws Exception {
        long id = json(agregar("unico@gmail.com")).path("id").asLong();

        mockMvc.perform(delete("/api/v1/notificaciones/" + id).header(HttpHeaders.AUTHORIZATION, adminToken()));

        assertThat(listar().path("usaRespaldo").asBoolean()).isTrue();
    }

    @Test
    void quitarUnaCasillaInexistenteDevuelve404() throws Exception {
        int status = mockMvc.perform(delete("/api/v1/notificaciones/999999")
                .header(HttpHeaders.AUTHORIZATION, adminToken())).andReturn().getResponse().getStatus();

        assertThat(status).isEqualTo(404);
    }

    @Test
    void mailDePruebaSeMandaALaCasillaElegida() throws Exception {
        long id = json(agregar("hermano@gmail.com")).path("id").asLong();

        MvcResult result = mockMvc.perform(post("/api/v1/notificaciones/" + id + "/prueba")
                .header(HttpHeaders.AUTHORIZATION, adminToken())).andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        verify(emailService).enviarPruebaNotificacion("hermano@gmail.com");
    }

    @Test
    void siElMailDePruebaFallaElPanelRecibeUnError() throws Exception {
        long id = json(agregar("hermano@gmail.com")).path("id").asLong();
        doThrow(new RuntimeException("SMTP caído")).when(emailService).enviarPruebaNotificacion(anyString());

        MvcResult result = mockMvc.perform(post("/api/v1/notificaciones/" + id + "/prueba")
                .header(HttpHeaders.AUTHORIZATION, adminToken())).andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(502);
        assertThat(json(result).path("error").asText()).isNotBlank();
    }

    @Test
    void mailDePruebaAUnaCasillaInexistenteDevuelve404() throws Exception {
        int status = mockMvc.perform(post("/api/v1/notificaciones/999999/prueba")
                .header(HttpHeaders.AUTHORIZATION, adminToken())).andReturn().getResponse().getStatus();

        assertThat(status).isEqualTo(404);
        verify(emailService, never()).enviarPruebaNotificacion(anyString());
    }
}
