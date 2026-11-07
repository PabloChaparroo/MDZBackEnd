package com.Capinteria.carpinteria.support;

import com.Capinteria.carpinteria.Entity.Categoria;
import com.Capinteria.carpinteria.Entity.Cliente;
import com.Capinteria.carpinteria.Entity.Usuario;
import com.Capinteria.carpinteria.Jwt.JwtService;
import com.Capinteria.carpinteria.Repositories.CategoriaRepository;
import com.Capinteria.carpinteria.Repositories.UsuarioRepository;
import com.Capinteria.carpinteria.Service.EmailService;
import com.Capinteria.carpinteria.enumeration.Role;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

/**
 * Base de los tests de integración: contexto Spring completo contra el Postgres real
 * (el mismo que usa el CI), con Flyway, seguridad y JPA de verdad.
 *
 * - Cada test arranca con la base vacía (TRUNCATE de todas las tablas de negocio).
 * - El envío de mails está reemplazado por un mock: ningún test manda mails reales.
 * - Todas las subclases comparten esta misma configuración, así Spring reutiliza un solo contexto.
 */
// El análisis de nulos de Eclipse marca los valores sin anotar que se pasan a APIs de Spring
// anotadas @NonNull (MediaType, String, ids...). Nunca son null acá; mismo criterio que el resto del proyecto.
@SuppressWarnings("null")
@SpringBootTest
@AutoConfigureMockMvc
public abstract class IntegrationTestBase {

    protected static final String ADMIN_USERNAME = "admin.test@mdz.local";
    protected static final String ADMIN_PASSWORD = "clave-admin-test";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected JdbcTemplate jdbc;

    @Autowired
    protected UsuarioRepository usuarioRepository;

    @Autowired
    protected CategoriaRepository categoriaRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected JwtService jwtService;

    @MockBean
    protected EmailService emailService;

    @BeforeEach
    void limpiarBase() {
        jdbc.execute("TRUNCATE TABLE consulta, mueble_imagenes, mueble, categoria, usuario, cliente, destinatario_notificacion RESTART IDENTITY CASCADE");
    }

    // ---------- Usuarios y tokens ----------

    protected Usuario crearUsuario(String username, String password, Role role) {
        Cliente cliente = Cliente.builder()
                .nombreCliente("Nombre " + role.name())
                .apellidoCliente("Apellido")
                .telefonoCliente("2610000000")
                .mailCliente(username)
                .fechaHoraAltaCliente(LocalDateTime.now())
                .build();
        Usuario usuario = Usuario.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .role(role)
                .fechaAltaUsuario(LocalDate.now())
                .build();
        usuario.setCliente(cliente);
        return usuarioRepository.save(usuario);
    }

    /** Crea el admin de prueba (si no existe) y devuelve el header "Bearer ..." listo para usar. */
    protected String adminToken() {
        Usuario admin = usuarioRepository.findByUsername(ADMIN_USERNAME)
                .orElseGet(() -> crearUsuario(ADMIN_USERNAME, ADMIN_PASSWORD, Role.ADMIN));
        return "Bearer " + jwtService.getToken(admin);
    }

    /** Token de un usuario logueado que NO es admin (rol CLIENTE). */
    protected String clienteToken() {
        String username = "cliente.test@mdz.local";
        Usuario cliente = usuarioRepository.findByUsername(username)
                .orElseGet(() -> crearUsuario(username, "clave-cliente", Role.CLIENTE));
        return "Bearer " + jwtService.getToken(cliente);
    }

    // ---------- Datos de negocio ----------

    protected Categoria crearCategoria(String nombre) {
        return categoriaRepository.save(Categoria.builder()
                .nombreCategoria(nombre)
                .fechaAltaCategoria(new Date())
                .build());
    }

    protected Categoria crearCategoriaDadaDeBaja(String nombre) {
        return categoriaRepository.save(Categoria.builder()
                .nombreCategoria(nombre)
                .fechaAltaCategoria(new Date())
                .fechaBajaCategoria(new Date())
                .build());
    }

    protected MockMultipartFile imagen(String contenido) {
        return new MockMultipartFile("files", contenido + ".jpg", "image/jpeg", contenido.getBytes());
    }

    /**
     * Crea un mueble por la API real (POST /api/v1/mueble/create) con N imágenes
     * y devuelve su id (lo busca por nombre, porque el endpoint no lo devuelve).
     */
    protected long crearMueble(String nombre, String color, long categoriaId, int cantidadImagenes,
                               Integer portadaIndex, boolean publicar) throws Exception {
        String json = objectMapper.writeValueAsString(java.util.Map.of(
                "nombreMueble", nombre,
                "colorMueble", color,
                "descripcion", "Descripción de " + nombre,
                "publicar", publicar));
        MockMultipartHttpServletRequestBuilder request = multipart("/api/v1/mueble/create");
        for (int i = 0; i < cantidadImagenes; i++) {
            request.file(imagen(nombre + "-img" + i));
        }
        request.param("mueble", json).param("categoriaId", String.valueOf(categoriaId));
        if (portadaIndex != null) {
            request.param("portadaIndex", String.valueOf(portadaIndex));
        }
        MvcResult result = mockMvc.perform(request.header(HttpHeaders.AUTHORIZATION, adminToken())).andReturn();
        if (result.getResponse().getStatus() != 200) {
            throw new AssertionError("No se pudo crear el mueble '" + nombre + "': "
                    + result.getResponse().getStatus() + " " + result.getResponse().getContentAsString());
        }
        Long id = jdbc.queryForObject("SELECT id FROM mueble WHERE nombre_mueble = ?", Long.class, nombre);
        return id;
    }

    protected long crearMueble(String nombre, long categoriaId) throws Exception {
        return crearMueble(nombre, "Natural", categoriaId, 1, 0, false);
    }

    // ---------- Utilidades ----------

    /** Lee el body como UTF-8 (MockMvc usa ISO-8859-1 si la respuesta no declara charset; el navegador usa UTF-8). */
    protected JsonNode json(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8));
    }

    protected JsonNode getJson(String url) throws Exception {
        MvcResult result = mockMvc.perform(get(url)).andReturn();
        if (result.getResponse().getStatus() != 200) {
            throw new AssertionError("GET " + url + " devolvió " + result.getResponse().getStatus()
                    + ": " + result.getResponse().getContentAsString());
        }
        return json(result);
    }

    protected String toJson(Object o) throws Exception {
        return objectMapper.writeValueAsString(o);
    }
}
