package com.Capinteria.carpinteria.Service.correo;

import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Properties;

/**
 * Envío por la API HTTP de Gmail (HTTPS, puerto 443), autenticado con OAuth 2.0 como la cuenta remitente.
 * Se usa en producción porque el plan gratis de Render bloquea los puertos SMTP (25, 465 y 587).
 *
 * Necesita GMAIL_CLIENT_ID, GMAIL_CLIENT_SECRET y GMAIL_REFRESH_TOKEN (ver scripts/obtener-token-gmail.js).
 * Con el refresh token pide un access token (dura ~1 hora, se reutiliza mientras no venza) y manda el mail
 * armado como MIME en base64url a users/me/messages/send.
 */
@SuppressWarnings("null")
@Component
@ConditionalOnProperty(name = "mdz.mail.proveedor", havingValue = "gmail-api")
public class GmailApiCorreoSender implements CorreoSender {

    static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    static final String SEND_URL = "https://gmail.googleapis.com/gmail/v1/users/me/messages/send";

    private final RestClient http;
    private final String clientId;
    private final String clientSecret;
    private final String refreshToken;

    private String accessToken;
    private Instant accessTokenVence = Instant.EPOCH;

    public GmailApiCorreoSender(RestClient.Builder builder,
                                @Value("${mdz.mail.gmail.client-id:}") String clientId,
                                @Value("${mdz.mail.gmail.client-secret:}") String clientSecret,
                                @Value("${mdz.mail.gmail.refresh-token:}") String refreshToken) {
        if (clientId.isBlank() || clientSecret.isBlank() || refreshToken.isBlank()) {
            throw new IllegalStateException("MAIL_PROVIDER=gmail-api necesita GMAIL_CLIENT_ID, GMAIL_CLIENT_SECRET "
                    + "y GMAIL_REFRESH_TOKEN (se obtienen con scripts/obtener-token-gmail.js).");
        }
        this.http = builder.build();
        this.clientId = clientId.trim();
        this.clientSecret = clientSecret.trim();
        this.refreshToken = refreshToken.trim();
    }

    @Override
    public void enviar(SimpleMailMessage mensaje) {
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(aMime(mensaje));
        try {
            http.post().uri(SEND_URL)
                    .header("Authorization", "Bearer " + accessToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("raw", raw))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RuntimeException e) {
            throw new MailSendException("La API de Gmail rechazó el envío: " + e.getMessage(), e);
        }
    }

    /** Access token vigente; si venció (o vence en menos de un minuto) pide uno nuevo con el refresh token. */
    synchronized String accessToken() {
        if (accessToken != null && Instant.now().isBefore(accessTokenVence.minusSeconds(60))) {
            return accessToken;
        }
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("refresh_token", refreshToken);
        form.add("grant_type", "refresh_token");
        Map<?, ?> respuesta;
        try {
            respuesta = http.post().uri(TOKEN_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(Map.class);
        } catch (RuntimeException e) {
            throw new MailSendException("No se pudo renovar el acceso a Gmail (¿se revocó la autorización o "
                    + "cambió la contraseña de la cuenta? Hay que generar un GMAIL_REFRESH_TOKEN nuevo): " + e.getMessage(), e);
        }
        if (respuesta == null || respuesta.get("access_token") == null) {
            throw new MailSendException("Google no devolvió un access token");
        }
        accessToken = respuesta.get("access_token").toString();
        long segundos = respuesta.get("expires_in") instanceof Number n ? n.longValue() : 3600;
        accessTokenVence = Instant.now().plusSeconds(segundos);
        return accessToken;
    }

    /** Arma el mail en formato MIME (con UTF-8, para tildes y eñes en asunto y cuerpo). */
    static byte[] aMime(SimpleMailMessage m) {
        try {
            MimeMessage mime = new MimeMessage(Session.getInstance(new Properties()));
            if (m.getFrom() != null && !m.getFrom().isBlank()) {
                mime.setFrom(new InternetAddress(m.getFrom()));
            }
            if (m.getTo() != null) {
                for (String to : m.getTo()) {
                    mime.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
                }
            }
            if (m.getReplyTo() != null && !m.getReplyTo().isBlank()) {
                mime.setReplyTo(InternetAddress.parse(m.getReplyTo()));
            }
            mime.setSubject(m.getSubject(), "UTF-8");
            mime.setText(m.getText() == null ? "" : m.getText(), "UTF-8");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            mime.writeTo(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new MailSendException("No se pudo armar el mail: " + e.getMessage(), e);
        }
    }
}
