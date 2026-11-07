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

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/** Endpoints de clientes y usuarios del panel (todos solo ADMIN). */
class ClienteUsuarioFlowTest extends IntegrationTestBase {

    private MvcResult crearCliente(String nombre, String apellido, String mail) throws Exception {
        return mockMvc.perform(post("/api/v1/cliente/crear")
                .header(HttpHeaders.AUTHORIZATION, adminToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of(
                        "nombreCliente", nombre,
                        "apellidoCliente", apellido,
                        "telefonoCliente", "2610000000",
                        "mailCliente", mail)))).andReturn();
    }

    private JsonNode adminGet(String url) throws Exception {
        MvcResult result = mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, adminToken())).andReturn();
        assertThat(result.getResponse().getStatus()).as("GET " + url).isEqualTo(200);
        return json(result);
    }

    // ---------- Cliente ----------

    @Test
    void crearClienteYBuscarloPorMailYPorId() throws Exception {
        MvcResult result = crearCliente("Sofía", "Ruiz", "sofia@mail.com");

        assertThat(result.getResponse().getStatus()).isEqualTo(201);
        long id = json(result).path("clienteId").asLong();
        assertThat(adminGet("/api/v1/cliente/byMailCliente?mailCliente=sofia@mail.com").path("id").asLong()).isEqualTo(id);
        assertThat(adminGet("/api/v1/cliente/searchById?idCliente=" + id).path("nombreCliente").asText()).isEqualTo("Sofía");
    }

    @Test
    void noSePuedenCrearDosClientesConElMismoMail() throws Exception {
        crearCliente("Sofía", "Ruiz", "sofia@mail.com");

        assertThat(crearCliente("Otra", "Persona", "sofia@mail.com").getResponse().getStatus()).isEqualTo(400);
    }

    @Test
    void crearClienteSinNombreEsRechazado() throws Exception {
        assertThat(crearCliente(" ", "Ruiz", "x@mail.com").getResponse().getStatus()).isEqualTo(400);
    }

    @Test
    void modificarClienteActualizaSusDatos() throws Exception {
        long id = json(crearCliente("Sofía", "Ruiz", "sofia@mail.com")).path("clienteId").asLong();

        MvcResult result = mockMvc.perform(put("/api/v1/cliente/modifyCliente")
                .header(HttpHeaders.AUTHORIZATION, adminToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of(
                        "idCliente", id,
                        "nombreCliente", "Sofía Inés",
                        "apellidoCliente", "Ruiz",
                        "telefonoCliente", "2619999999",
                        "mailCliente", "sofia.ines@mail.com")))).andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        JsonNode cliente = adminGet("/api/v1/cliente/" + id);
        assertThat(cliente.path("nombreCliente").asText()).isEqualTo("Sofía Inés");
        assertThat(cliente.path("mailCliente").asText()).isEqualTo("sofia.ines@mail.com");
    }

    @Test
    void verPerfilDevuelveLosDatosDelAdminLogueado() throws Exception {
        JsonNode perfil = adminGet("/api/v1/cliente/showProfile");

        assertThat(perfil.path("username").asText()).isEqualTo(ADMIN_USERNAME);
        assertThat(perfil.path("mailCliente").asText()).isEqualTo(ADMIN_USERNAME);
    }

    @Test
    void darDeBajaUnClienteConUsuarioMarcaLaBaja() throws Exception {
        Usuario usuario = crearUsuario("empleado@mdz.local", "clave", Role.EMPLEADO);
        long clienteId = usuario.getCliente().getId();

        MvcResult result = mockMvc.perform(put("/api/v1/cliente/deleteCliente")
                .header(HttpHeaders.AUTHORIZATION, adminToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.valueOf(clienteId))).andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        assertThat(jdbc.queryForObject("SELECT fecha_hora_baja_cliente FROM cliente WHERE id = ?", Object.class, clienteId)).isNotNull();
    }

    @Test
    void darDeBajaUnClienteTambienDaDeBajaSuUsuario() throws Exception {
        // El usuario no se guarda explícitamente: se persiste porque el save del cliente hace flush
        // de la sesión abierta durante el request. Este test cuida que eso no se rompa.
        Usuario usuario = crearUsuario("empleado@mdz.local", "clave", Role.EMPLEADO);

        mockMvc.perform(put("/api/v1/cliente/deleteCliente")
                .header(HttpHeaders.AUTHORIZATION, adminToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.valueOf(usuario.getCliente().getId()))).andReturn();

        assertThat(jdbc.queryForObject("SELECT fecha_baja_usuario FROM usuario WHERE id = ?", Object.class, usuario.getId())).isNotNull();
    }

    @Test
    @Disabled("BUG: ClienteServiceImpl.deleteCliente asume que todo cliente tiene un Usuario. Los clientes que "
            + "se crean desde los formularios de consulta no lo tienen, y la baja falla con NullPointerException (400).")
    void darDeBajaUnClienteSinUsuarioFunciona() throws Exception {
        long id = json(crearCliente("Sofía", "Ruiz", "sofia@mail.com")).path("clienteId").asLong();

        MvcResult result = mockMvc.perform(put("/api/v1/cliente/deleteCliente")
                .header(HttpHeaders.AUTHORIZATION, adminToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.valueOf(id))).andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    // ---------- Usuario ----------

    @Test
    void buscarUsuarioPorUsername() throws Exception {
        crearUsuario("empleado@mdz.local", "clave", Role.EMPLEADO);

        JsonNode usuario = adminGet("/api/v1/usuarios/findUserByUsername?username=empleado@mdz.local");

        assertThat(usuario.path("username").asText()).isEqualTo("empleado@mdz.local");
        assertThat(usuario.path("role").asText()).isEqualTo("EMPLEADO");
    }

    @Test
    void listarUsuariosNoExponeLasClavesEnTextoPlano() throws Exception {
        crearUsuario("empleado@mdz.local", "clave-secreta", Role.EMPLEADO);

        String body = mockMvc.perform(get("/api/v1/usuarios").header(HttpHeaders.AUTHORIZATION, adminToken()))
                .andReturn().getResponse().getContentAsString();

        assertThat(body).contains("empleado@mdz.local").doesNotContain("clave-secreta");
    }
}
