package com.Capinteria.carpinteria.security;

import com.Capinteria.carpinteria.Config.SecurityConfig;
import com.Capinteria.carpinteria.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;

/**
 * CORS: solo los orígenes de cors.allowed-origins (en tests, https://front.mdz.test) pueden llamar a la API.
 */
// El análisis de nulos de Eclipse marca los valores sin anotar que se pasan a APIs de Spring
// anotadas @NonNull (MediaType, String, ids...). Nunca son null acá; mismo criterio que el resto del proyecto.
@SuppressWarnings("null")
class CorsTest extends IntegrationTestBase {

    private static final String FRONT = "https://front.mdz.test";
    private static final String OTRO = "https://sitio-malicioso.example";

    private MvcResult preflight(String origen, String metodo, String headers) throws Exception {
        return mockMvc.perform(options("/api/v1/categoria/crear")
                .header(HttpHeaders.ORIGIN, origen)
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, metodo)
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, headers)).andReturn();
    }

    @Test
    void preflightDesdeElFrontPermitidoRecibeLosHeadersCors() throws Exception {
        MvcResult result = preflight(FRONT, "POST", "authorization,content-type");

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        assertThat(result.getResponse().getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN)).isEqualTo(FRONT);
        assertThat(result.getResponse().getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS)).contains("POST");
        assertThat(result.getResponse().getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS))
                .containsIgnoringCase("authorization").containsIgnoringCase("content-type");
        // Sin cookies: no se habilitan credenciales
        assertThat(result.getResponse().getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS)).isNull();
    }

    @Test
    void preflightDesdeOtroOrigenEsRechazado() throws Exception {
        MvcResult result = preflight(OTRO, "POST", "authorization,content-type");

        assertThat(result.getResponse().getStatus()).isEqualTo(403);
        assertThat(result.getResponse().getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN)).isNull();
    }

    @Test
    void getRealDesdeElFrontTraeElHeaderCors() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/categoria/todas").header(HttpHeaders.ORIGIN, FRONT)).andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        assertThat(result.getResponse().getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN)).isEqualTo(FRONT);
    }

    @Test
    void getRealDesdeOtroOrigenNoTraeElHeaderCors() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/categoria/todas").header(HttpHeaders.ORIGIN, OTRO)).andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(403);
        assertThat(result.getResponse().getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN)).isNull();
    }

    @Test
    void metodoNoPermitidoEsRechazado() throws Exception {
        assertThat(preflight(FRONT, "PATCH", "content-type").getResponse().getStatus()).isEqualTo(403);
    }

    @Test
    void sinOrigenesConfiguradosNoSePermiteNinguno() {
        // Producción sin CORS_ALLOWED_ORIGINS: falla cerrado
        CorsConfiguration config = new SecurityConfig(null, null)
                .corsConfigurationSource(List.of())
                .getCorsConfiguration(new MockHttpServletRequest("GET", "/api/v1/categoria/todas"));

        assertThat(config.checkOrigin(FRONT)).isNull();
        assertThat(config.checkOrigin("http://localhost:5173")).isNull();
    }

    @Test
    void variosOrigenesSeparadosPorComaConEspacios() {
        CorsConfiguration config = new SecurityConfig(null, null)
                .corsConfigurationSource(List.of("https://a.pages.dev", " https://www.mdz.com "))
                .getCorsConfiguration(new MockHttpServletRequest("GET", "/x"));

        assertThat(config.checkOrigin("https://a.pages.dev")).isEqualTo("https://a.pages.dev");
        assertThat(config.checkOrigin("https://www.mdz.com")).isEqualTo("https://www.mdz.com");
        assertThat(config.checkOrigin(OTRO)).isNull();
    }
}
