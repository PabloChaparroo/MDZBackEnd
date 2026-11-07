package com.Capinteria.carpinteria.security;

import com.Capinteria.carpinteria.support.IntegrationTestBase;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;

/**
 * Matriz de seguridad: para cada endpoint del sistema verifica quién puede entrar.
 *
 * - PUBLICO: responde sin token (no 401/403).
 * - ADMIN: sin token -> 401/403, con token de un usuario CLIENTE -> 403, con token de ADMIN -> pasa la seguridad.
 *
 * "Pasa la seguridad" = la respuesta no es 401/403. El resultado de negocio (200, 400, 404...)
 * no importa acá; eso lo cubren los tests de flujo de cada módulo. Si el controlador explota
 * con una excepción no manejada, también cuenta como que pasó la seguridad (llegó al controlador).
 *
 * Si alguien cambia SecurityConfig y abre o cierra una ruta sin querer, este test falla.
 */
class SecurityMatrixTest extends IntegrationTestBase {

    private static final String ID = "999999"; // id que no existe: nada real se modifica ni se borra

    enum Acceso { PUBLICO, ADMIN }

    static Stream<Arguments> endpoints() {
        return Stream.of(
                // ---------- Health ----------
                ep(Acceso.PUBLICO, "GET", "/ping", null),

                // ---------- Auth (todo público) ----------
                ep(Acceso.PUBLICO, "POST", "/auth/login", "{\"username\":\"x\",\"password\":\"y\"}"),
                // /auth/refresh-token no va acá: con un token inválido responde 401 por diseño.
                // Que es público lo prueba AuthFlowTest con un refresh token válido y sin access token.
                ep(Acceso.PUBLICO, "POST", "/auth/register", "{\"username\":\"nuevo.matriz@mdz.local\",\"password\":\"x\"}"),
                ep(Acceso.PUBLICO, "POST", "/auth/registerEmployee", "{\"username\":\"empleado.matriz@mdz.local\",\"provisionalPassword\":\"x\"}"),
                // Endpoints de diagnóstico: públicos. Candidatos a borrar antes de producción.
                ep(Acceso.PUBLICO, "GET", "/auth/test", null),
                ep(Acceso.PUBLICO, "POST", "/auth/test-post", null),
                ep(Acceso.PUBLICO, "POST", "/auth/login-test", "{\"username\":\"x\",\"password\":\"y\"}"),

                // ---------- Mueble: lecturas públicas ----------
                ep(Acceso.PUBLICO, "GET", "/api/v1/mueble/" + ID, null),
                ep(Acceso.PUBLICO, "GET", "/api/v1/mueble/page/0", null),
                ep(Acceso.PUBLICO, "GET", "/api/v1/mueble/obtener-todos-muebles/0", null),
                ep(Acceso.PUBLICO, "GET", "/api/v1/mueble/muebles-por-categoria/0/categoria/" + ID, null),
                ep(Acceso.PUBLICO, "GET", "/api/v1/mueble/destacados", null),
                ep(Acceso.PUBLICO, "GET", "/api/v1/mueble/novedades/0", null),
                ep(Acceso.PUBLICO, "GET", "/api/v1/mueble/catalogo-completo/0", null),
                ep(Acceso.PUBLICO, "GET", "/api/v1/mueble/" + ID + "/imagenes", null),
                ep(Acceso.PUBLICO, "GET", "/api/v1/mueble/filtrar/buscar/mesa/0", null),
                // Listado de muebles dados de baja: hoy es público por la regla GET /api/v1/mueble/**
                ep(Acceso.PUBLICO, "GET", "/api/v1/mueble/dados-de-baja/0", null),

                // ---------- Mueble: escritura y listado completo solo ADMIN ----------
                ep(Acceso.ADMIN, "GET", "/api/v1/mueble", null),
                ep(Acceso.ADMIN, "POST", "/api/v1/mueble", "{}"),
                ep(Acceso.ADMIN, "PUT", "/api/v1/mueble", "[]"),
                ep(Acceso.ADMIN, "PUT", "/api/v1/mueble/" + ID, "{}"),
                ep(Acceso.ADMIN, "DELETE", "/api/v1/mueble/" + ID, null),
                ep(Acceso.ADMIN, "POST", "/api/v1/mueble/create", null),
                ep(Acceso.ADMIN, "PUT", "/api/v1/mueble/" + ID + "/imagenes", null),
                ep(Acceso.ADMIN, "PUT", "/api/v1/mueble/imagenes", null),
                ep(Acceso.ADMIN, "PUT", "/api/v1/mueble/modificar/" + ID, "{}"),
                ep(Acceso.ADMIN, "DELETE", "/api/v1/mueble/baja-logica/" + ID, null),
                ep(Acceso.ADMIN, "POST", "/api/v1/mueble/" + ID + "/agregar-imagenes", null),
                ep(Acceso.ADMIN, "DELETE", "/api/v1/mueble/imagen/" + ID, null),
                ep(Acceso.ADMIN, "PUT", "/api/v1/mueble/imagen/" + ID + "/establecer-portada", null),

                // ---------- Categoría ----------
                ep(Acceso.PUBLICO, "GET", "/api/v1/categoria", null),
                ep(Acceso.PUBLICO, "GET", "/api/v1/categoria/" + ID, null),
                ep(Acceso.PUBLICO, "GET", "/api/v1/categoria/todas", null),
                ep(Acceso.PUBLICO, "GET", "/api/v1/categoria/dadas-de-baja", null),
                ep(Acceso.ADMIN, "POST", "/api/v1/categoria", "{}"),
                ep(Acceso.ADMIN, "PUT", "/api/v1/categoria", "[]"),
                ep(Acceso.ADMIN, "PUT", "/api/v1/categoria/" + ID, "{}"),
                ep(Acceso.ADMIN, "DELETE", "/api/v1/categoria/" + ID, null),
                ep(Acceso.ADMIN, "POST", "/api/v1/categoria/crear", "{\"nombreCategoria\":\"Matriz\"}"),
                ep(Acceso.ADMIN, "PUT", "/api/v1/categoria/modificar/" + ID, "{\"nombreCategoria\":\"Matriz\"}"),
                ep(Acceso.ADMIN, "DELETE", "/api/v1/categoria/baja-logica/" + ID, null),

                // ---------- Imágenes ----------
                ep(Acceso.PUBLICO, "GET", "/api/v1/muebleImagenes", null),
                ep(Acceso.PUBLICO, "GET", "/api/v1/muebleImagenes/" + ID, null),
                ep(Acceso.ADMIN, "POST", "/api/v1/muebleImagenes", "{}"),
                ep(Acceso.ADMIN, "PUT", "/api/v1/muebleImagenes", "[]"),
                ep(Acceso.ADMIN, "PUT", "/api/v1/muebleImagenes/" + ID, "{}"),
                ep(Acceso.ADMIN, "DELETE", "/api/v1/muebleImagenes/" + ID, null),
                ep(Acceso.ADMIN, "POST", "/api/v1/muebleImagenes/imagenes", null),
                ep(Acceso.ADMIN, "PUT", "/api/v1/muebleImagenes/imagenes", null),

                // ---------- Consultas: crear es público (formularios), el resto ADMIN ----------
                ep(Acceso.PUBLICO, "POST", "/api/v1/consultas/crear", "{}"),
                ep(Acceso.ADMIN, "GET", "/api/v1/consultas", null),
                ep(Acceso.ADMIN, "GET", "/api/v1/consultas/" + ID, null),
                ep(Acceso.ADMIN, "GET", "/api/v1/consultas/obtener-consultas/0", null),
                ep(Acceso.ADMIN, "GET", "/api/v1/consultas/filtrar-por-nombre?nombre=a", null),
                ep(Acceso.ADMIN, "PUT", "/api/v1/consultas/" + ID + "/responder", "{\"respuesta\":\"x\"}"),
                ep(Acceso.ADMIN, "POST", "/api/v1/consultas", "{}"),
                ep(Acceso.ADMIN, "PUT", "/api/v1/consultas", "[]"),
                ep(Acceso.ADMIN, "PUT", "/api/v1/consultas/" + ID, "{}"),
                ep(Acceso.ADMIN, "DELETE", "/api/v1/consultas/" + ID, null),

                // ---------- Cliente: todo ADMIN ----------
                ep(Acceso.ADMIN, "GET", "/api/v1/cliente", null),
                ep(Acceso.ADMIN, "GET", "/api/v1/cliente/" + ID, null),
                ep(Acceso.ADMIN, "POST", "/api/v1/cliente/crear", "{}"),
                ep(Acceso.ADMIN, "GET", "/api/v1/cliente/searchById?idCliente=" + ID, null),
                ep(Acceso.ADMIN, "GET", "/api/v1/cliente/showProfile", null),
                ep(Acceso.ADMIN, "PUT", "/api/v1/cliente/updateProfile", "{}"),
                ep(Acceso.ADMIN, "PUT", "/api/v1/cliente/modifyCliente", "{\"idCliente\":" + ID + "}"),
                ep(Acceso.ADMIN, "PUT", "/api/v1/cliente/deleteCliente", ID),
                ep(Acceso.ADMIN, "GET", "/api/v1/cliente/byMailCliente?mailCliente=x@x.com", null),
                ep(Acceso.ADMIN, "POST", "/api/v1/cliente", "{}"),
                ep(Acceso.ADMIN, "PUT", "/api/v1/cliente", "[]"),
                ep(Acceso.ADMIN, "PUT", "/api/v1/cliente/" + ID, "{}"),
                ep(Acceso.ADMIN, "DELETE", "/api/v1/cliente/" + ID, null),

                // ---------- Usuarios: todo ADMIN ----------
                ep(Acceso.ADMIN, "GET", "/api/v1/usuarios", null),
                ep(Acceso.ADMIN, "GET", "/api/v1/usuarios/" + ID, null),
                ep(Acceso.ADMIN, "GET", "/api/v1/usuarios/findUserByUsername?username=nadie", null),
                ep(Acceso.ADMIN, "GET", "/api/v1/usuarios/showProfile", null),
                ep(Acceso.ADMIN, "POST", "/api/v1/usuarios", "{}"),
                ep(Acceso.ADMIN, "PUT", "/api/v1/usuarios", "[]"),
                ep(Acceso.ADMIN, "PUT", "/api/v1/usuarios/" + ID, "{}"),
                ep(Acceso.ADMIN, "DELETE", "/api/v1/usuarios/" + ID, null),

                // ---------- Notificaciones: todo ADMIN ----------
                ep(Acceso.ADMIN, "GET", "/api/v1/notificaciones", null),
                ep(Acceso.ADMIN, "POST", "/api/v1/notificaciones", "{\"mail\":\"matriz@mdz.test\"}"),
                ep(Acceso.ADMIN, "DELETE", "/api/v1/notificaciones/" + ID, null),
                ep(Acceso.ADMIN, "POST", "/api/v1/notificaciones/" + ID + "/prueba", null)
        );
    }

    private static Arguments ep(Acceso acceso, String metodo, String url, String body) {
        return Arguments.of(acceso, metodo, url, body);
    }

    @ParameterizedTest(name = "{0} {1} {2}")
    @MethodSource("endpoints")
    void sinToken(Acceso acceso, String metodo, String url, String body) {
        int status = ejecutar(metodo, url, body, null);
        if (acceso == Acceso.PUBLICO) {
            assertThat(status).as("%s %s tiene que ser público", metodo, url).isNotIn(401, 403);
        } else {
            assertThat(status).as("%s %s no puede ser accesible sin token", metodo, url).isIn(401, 403);
        }
    }

    @ParameterizedTest(name = "{0} {1} {2}")
    @MethodSource("endpoints")
    void conTokenDeCliente(Acceso acceso, String metodo, String url, String body) {
        int status = ejecutar(metodo, url, body, clienteToken());
        if (acceso == Acceso.ADMIN) {
            assertThat(status).as("%s %s no puede ser accesible para un usuario no admin", metodo, url).isEqualTo(403);
        } else {
            assertThat(status).as("%s %s tiene que ser público también con token", metodo, url).isNotIn(401, 403);
        }
    }

    @ParameterizedTest(name = "{0} {1} {2}")
    @MethodSource("endpoints")
    void conTokenDeAdmin(Acceso acceso, String metodo, String url, String body) {
        int status = ejecutar(metodo, url, body, adminToken());
        assertThat(status).as("%s %s tiene que ser accesible para el admin", metodo, url).isNotIn(401, 403);
    }

    /** Devuelve el status HTTP; si el controlador lanza una excepción no manejada, devuelve 500 (pasó la seguridad). */
    private int ejecutar(String metodo, String url, String body, String token) {
        MockHttpServletRequestBuilder req = request(HttpMethod.valueOf(metodo), url);
        if (body != null) {
            req.contentType(MediaType.APPLICATION_JSON).content(body);
        }
        if (token != null) {
            req.header(HttpHeaders.AUTHORIZATION, token);
        }
        try {
            return mockMvc.perform(req).andReturn().getResponse().getStatus();
        } catch (Exception e) {
            return 500;
        }
    }
}
