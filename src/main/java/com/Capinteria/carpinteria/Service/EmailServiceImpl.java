package com.Capinteria.carpinteria.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;

    @Value("${mdz.mail.from}")
    private String remitente;

    @Value("${mdz.mail.admin}")
    private String mailAdministrador;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void enviarRespuestaConsulta(String mailDestino, String nombreCliente, String mensajeConsulta, String respuesta) {
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setFrom(remitente);
            mensaje.setTo(mailDestino);
            mensaje.setSubject("MDZ Muebles - Respuesta a tu consulta");
            mensaje.setText(
                    "Hola " + nombreCliente + ",\n\n" +
                    "Te respondemos tu consulta:\n\"" + mensajeConsulta + "\"\n\n" +
                    "Nuestra respuesta:\n" + respuesta + "\n\n" +
                    "Saludos,\nEquipo de MDZ Muebles"
            );
            mailSender.send(mensaje);
            logger.info("✅ [EMAIL] Respuesta enviada a: {}", mailDestino);
        } catch (Exception e) {
            logger.error("❌ [EMAIL] Error al enviar respuesta a {}: {}", mailDestino, e.getMessage());
            throw new RuntimeException("No se pudo enviar el email con la respuesta: " + e.getMessage(), e);
        }
    }

    @Override
    public void notificarNuevaConsulta(String nombreCliente, String mailCliente, int telefonoCliente,
                                       String mensajeConsulta, String nombreMueble) {
        CompletableFuture.runAsync(() -> {
            try {
                SimpleMailMessage mensaje = new SimpleMailMessage();
                mensaje.setFrom(remitente);
                mensaje.setTo(mailAdministrador);
                mensaje.setReplyTo(mailCliente);
                mensaje.setSubject(nombreMueble != null
                        ? "MDZ Muebles - Consulta por " + nombreMueble
                        : "MDZ Muebles - Nueva consulta general");
                mensaje.setText(
                        "Entró una nueva consulta desde la web.\n\n" +
                        "Nombre: " + nombreCliente + "\n" +
                        "Email: " + mailCliente + "\n" +
                        "Teléfono: " + (telefonoCliente != 0 ? telefonoCliente : "No indicado") + "\n" +
                        "Mueble: " + (nombreMueble != null ? nombreMueble : "Consulta general") + "\n\n" +
                        "Mensaje:\n" + mensajeConsulta
                );
                mailSender.send(mensaje);
                logger.info("✅ [EMAIL] Aviso de nueva consulta enviado");
            } catch (Exception e) {
                logger.error("❌ [EMAIL] Error al avisar la nueva consulta: {}", e.getMessage());
            }
        });
    }
}
