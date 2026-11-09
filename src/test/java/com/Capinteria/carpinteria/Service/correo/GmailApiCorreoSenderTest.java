package com.Capinteria.carpinteria.Service.correo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/** Envío por la API de Gmail, con Google simulado (no sale nada a internet). */
@SuppressWarnings("null")
class GmailApiCorreoSenderTest {

    private static final String TOKEN_OK = "{\"access_token\":\"ya29.token-de-prueba\",\"expires_in\":3599,\"token_type\":\"Bearer\"}";

    private MockRestServiceServer google;
    private GmailApiCorreoSender sender;
    private final List<String> rawsEnviados = new ArrayList<>();

    @BeforeEach
    void preparar() {
        RestClient.Builder builder = RestClient.builder();
        google = MockRestServiceServer.bindTo(builder).build();
        sender = new GmailApiCorreoSender(builder, "client-id", "client-secret", "refresh-token");
    }

    private SimpleMailMessage mensaje() {
        SimpleMailMessage m = new SimpleMailMessage();
        m.setFrom("MDZ Muebles <mdzmuebles.web@gmail.com>");
        m.setTo("hermano@gmail.com", "ventas@mdz.com");
        m.setReplyTo("cliente@test.com");
        m.setSubject("MDZ Muebles - Consulta por Mesa ratona");
        m.setText("Entró una consulta: ¿hacen envíos a Mendoza? Ñandú");
        return m;
    }

    private void esperarToken() {
        google.expect(requestTo(GmailApiCorreoSender.TOKEN_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().formDataContains(java.util.Map.of(
                        "grant_type", "refresh_token",
                        "refresh_token", "refresh-token",
                        "client_id", "client-id",
                        "client_secret", "client-secret")))
                .andRespond(withSuccess(TOKEN_OK, MediaType.APPLICATION_JSON));
    }

    private void esperarEnvio() {
        google.expect(requestTo(GmailApiCorreoSender.SEND_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer ya29.token-de-prueba"))
                .andExpect(req -> {
                    JsonNode body = new ObjectMapper().readTree(((MockClientHttpRequest) req).getBodyAsString());
                    rawsEnviados.add(body.path("raw").asText());
                })
                .andRespond(withSuccess("{\"id\":\"abc\"}", MediaType.APPLICATION_JSON));
    }

    private MimeMessage decodificar(String raw) throws Exception {
        byte[] bytes = Base64.getUrlDecoder().decode(raw);
        return new MimeMessage(Session.getInstance(new Properties()), new ByteArrayInputStream(bytes));
    }

    @Test
    void mandaElMailComoMimeConTodosLosDatosYTildes() throws Exception {
        esperarToken();
        esperarEnvio();

        sender.enviar(mensaje());

        google.verify();
        assertThat(rawsEnviados).hasSize(1);
        assertThat(rawsEnviados.get(0)).doesNotContain("+", "/", "="); // base64url sin relleno
        MimeMessage mime = decodificar(rawsEnviados.get(0));
        assertThat(mime.getFrom()[0].toString()).contains("mdzmuebles.web@gmail.com");
        assertThat(mime.getAllRecipients()).extracting(Object::toString)
                .containsExactly("hermano@gmail.com", "ventas@mdz.com");
        assertThat(mime.getReplyTo()[0].toString()).isEqualTo("cliente@test.com");
        assertThat(mime.getSubject()).isEqualTo("MDZ Muebles - Consulta por Mesa ratona");
        assertThat(mime.getContent().toString()).contains("¿hacen envíos a Mendoza? Ñandú");
    }

    @Test
    void reutilizaElAccessTokenMientrasNoVence() {
        esperarToken();     // un solo pedido de token...
        esperarEnvio();
        esperarEnvio();     // ...para dos mails

        sender.enviar(mensaje());
        sender.enviar(mensaje());

        google.verify();
    }

    @Test
    void siGoogleRechazaElRefreshTokenElErrorLoExplica() {
        google.expect(requestTo(GmailApiCorreoSender.TOKEN_URL))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\":\"invalid_grant\"}"));

        assertThatThrownBy(() -> sender.enviar(mensaje()))
                .isInstanceOf(MailSendException.class)
                .hasMessageContaining("GMAIL_REFRESH_TOKEN");
    }

    @Test
    void siGmailRechazaElEnvioLanzaExcepcion() {
        esperarToken();
        google.expect(requestTo(GmailApiCorreoSender.SEND_URL))
                .andRespond(withStatus(HttpStatus.FORBIDDEN));

        // Importante: la respuesta a una consulta no se guarda si el mail no salió
        assertThatThrownBy(() -> sender.enviar(mensaje())).isInstanceOf(MailSendException.class);
    }

    @Test
    void sinCredencialesLaAppNoArranca() {
        assertThatThrownBy(() -> new GmailApiCorreoSender(RestClient.builder(), "", "s", "r"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("GMAIL_CLIENT_ID");
    }
}
