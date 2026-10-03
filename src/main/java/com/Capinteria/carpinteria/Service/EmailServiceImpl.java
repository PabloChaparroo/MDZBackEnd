package com.Capinteria.carpinteria.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;

    @Value("${mdz.mail.from}")
    private String remitente;

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
}
