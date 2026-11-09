package com.Capinteria.carpinteria.security;

import com.Capinteria.carpinteria.Entity.Usuario;
import com.Capinteria.carpinteria.Jwt.JwtService;
import com.Capinteria.carpinteria.Repositories.UsuarioRepository;
import com.Capinteria.carpinteria.Service.EmailService;
import com.Capinteria.carpinteria.enumeration.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Con servidor HTTP real (no MockMvc): un error de un controller pasa por el reenvío interno a /error.
 * Si /error no fuera público, la seguridad lo convertiría en 403. MockMvc no hace ese reenvío,
 * por eso este caso necesita un servidor de verdad.
 */
// El análisis de nulos de Eclipse marca los valores sin anotar que se pasan a APIs de Spring
// anotadas @NonNull (MediaType, String, ids...). Nunca son null acá; mismo criterio que el resto del proyecto.
@SuppressWarnings("null")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ErroresServidorRealTest {

    @LocalServerPort
    private int port;

    @Autowired private JdbcTemplate jdbc;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtService jwtService;

    @MockBean
    private EmailService emailService;

    private final HttpClient http = HttpClient.newHttpClient();
    private String token;

    @BeforeEach
    void preparar() {
        jdbc.execute("TRUNCATE TABLE consulta, mueble_imagenes, mueble, categoria, usuario, cliente, destinatario_notificacion RESTART IDENTITY CASCADE");
        Usuario admin = usuarioRepository.save(Usuario.builder()
                .username("admin.real@mdz.local")
                .password(passwordEncoder.encode("x"))
                .role(Role.ADMIN)
                .fechaAltaUsuario(LocalDate.now())
                .build());
        token = jwtService.getToken(admin);
    }

    private int status(String metodo, String path, String body, boolean conToken) throws Exception {
        HttpRequest.Builder req = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json")
                .method(metodo, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
        if (conToken) req.header("Authorization", "Bearer " + token);
        return http.send(req.build(), HttpResponse.BodyHandlers.discarding()).statusCode();
    }

    @Test
    void unDuplicadoDevuelve409YNo403() throws Exception {
        assertThat(status("POST", "/api/v1/notificaciones", "{\"mail\":\"a@mdz.com\"}", true)).isEqualTo(201);

        assertThat(status("POST", "/api/v1/notificaciones", "{\"mail\":\"a@mdz.com\"}", true)).isEqualTo(409);
    }

    @Test
    void unMailInvalidoDevuelve400() throws Exception {
        assertThat(status("POST", "/api/v1/notificaciones", "{\"mail\":\"x\"}", true)).isEqualTo(400);
    }

    @Test
    void unRecursoInexistenteDevuelve404() throws Exception {
        assertThat(status("DELETE", "/api/v1/notificaciones/999999", null, true)).isEqualTo(404);
    }

    @Test
    void unJsonMalFormadoEnUnEndpointPublicoDevuelve400() throws Exception {
        assertThat(status("POST", "/api/v1/consultas/crear", "{esto no es json", false)).isEqualTo(400);
    }

    @Test
    void permitirErrorNoAbreLasRutasProtegidas() throws Exception {
        assertThat(status("GET", "/api/v1/notificaciones", null, false)).isEqualTo(403);
        assertThat(status("GET", "/api/v1/cliente", null, false)).isEqualTo(403);
    }
}
