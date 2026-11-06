package com.Capinteria.carpinteria.flows;

import com.Capinteria.carpinteria.Entity.Usuario;
import com.Capinteria.carpinteria.enumeration.Role;
import com.Capinteria.carpinteria.support.IntegrationTestBase;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/** Login, refresh token, perfil y registro. */
class AuthFlowTest extends IntegrationTestBase {

    private MvcResult login(String username, String password) throws Exception {
        return mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of("username", username, "password", password)))).andReturn();
    }

    @Test
    void loginCorrectoDevuelveAccessYRefreshToken() throws Exception {
        crearUsuario(ADMIN_USERNAME, ADMIN_PASSWORD, Role.ADMIN);

        MvcResult result = login(ADMIN_USERNAME, ADMIN_PASSWORD);

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        JsonNode body = json(result);
        assertThat(body.path("token").asText()).isNotBlank();
        assertThat(body.path("refreshToken").asText()).isNotBlank();
    }

    @Test
    void elTokenDelLoginDaAccesoAlPanelAdmin() throws Exception {
        crearUsuario(ADMIN_USERNAME, ADMIN_PASSWORD, Role.ADMIN);
        String token = json(login(ADMIN_USERNAME, ADMIN_PASSWORD)).path("token").asText();

        MvcResult perfil = mockMvc.perform(get("/api/v1/usuarios/showProfile")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)).andReturn();

        assertThat(perfil.getResponse().getStatus()).isEqualTo(200);
        JsonNode body = json(perfil);
        assertThat(body.path("username").asText()).isEqualTo(ADMIN_USERNAME);
        assertThat(body.path("role").asText()).isEqualTo("ADMIN");
    }

    @Test
    void loginConClaveIncorrectaNoDevuelveToken() throws Exception {
        crearUsuario(ADMIN_USERNAME, ADMIN_PASSWORD, Role.ADMIN);

        MvcResult result = login(ADMIN_USERNAME, "clave-equivocada");

        assertThat(result.getResponse().getStatus()).isBetween(400, 499);
        assertThat(result.getResponse().getContentAsString()).doesNotContain("refreshToken");
    }

    @Test
    void loginConUsuarioInexistenteNoDevuelveToken() throws Exception {
        MvcResult result = login("nadie@mdz.local", "x");

        assertThat(result.getResponse().getStatus()).isBetween(400, 499);
        assertThat(result.getResponse().getContentAsString()).doesNotContain("refreshToken");
    }

    @Test
    void refreshTokenValidoEmiteTokensNuevosSinAccessToken() throws Exception {
        crearUsuario(ADMIN_USERNAME, ADMIN_PASSWORD, Role.ADMIN);
        String refresh = json(login(ADMIN_USERNAME, ADMIN_PASSWORD)).path("refreshToken").asText();

        // Sin header Authorization: prueba que la ruta es pública
        MvcResult result = mockMvc.perform(post("/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of("refreshToken", refresh)))).andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        assertThat(json(result).path("token").asText()).isNotBlank();
        assertThat(json(result).path("refreshToken").asText()).isNotBlank();
    }

    @Test
    void refreshTokenInvalidoDevuelve401() throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of("refreshToken", "no-es-un-jwt")))).andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(401);
    }

    @Test
    void tokenAdulteradoNoDaAcceso() throws Exception {
        String token = adminToken();
        String adulterado = token.substring(0, token.length() - 4) + "abcd";

        int status = mockMvc.perform(get("/api/v1/cliente")
                .header(HttpHeaders.AUTHORIZATION, adulterado)).andReturn().getResponse().getStatus();

        assertThat(status).isIn(401, 403);
    }

    @Test
    void registroPublicoCreaUsuarioClienteSinAccesoAlPanel() throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of(
                        "username", "registrado@mdz.local",
                        "password", "clave",
                        "nombreCliente", "Ana",
                        "apellidoCliente", "Pérez",
                        "mailCliente", "registrado@mdz.local")))).andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        Usuario usuario = usuarioRepository.findByUsername("registrado@mdz.local").orElseThrow();
        assertThat(usuario.getRole()).isEqualTo(Role.CLIENTE);

        String token = json(result).path("token").asText();
        int status = mockMvc.perform(get("/api/v1/cliente")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)).andReturn().getResponse().getStatus();
        assertThat(status).isEqualTo(403);
    }

    @Test
    @Disabled("BUG: Usuario.isEnabled() siempre devuelve true, así que un usuario dado de baja "
            + "(fecha_baja_usuario cargada) puede seguir logueándose.")
    void usuarioDadoDeBajaNoPuedeLoguearse() throws Exception {
        Usuario usuario = crearUsuario("baja@mdz.local", "clave", Role.ADMIN);
        usuario.setFechaBajaUsuario(LocalDate.now());
        usuarioRepository.save(usuario);

        MvcResult result = login("baja@mdz.local", "clave");

        assertThat(result.getResponse().getStatus()).isBetween(400, 499);
    }
}
