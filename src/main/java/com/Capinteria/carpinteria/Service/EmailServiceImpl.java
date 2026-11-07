package com.Capinteria.carpinteria.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.scheduling.annotation.Async;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;


@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;

    @Value("${mdz.mail.from}")
    private String remitente;

    /** Casillas que reciben el aviso de consulta nueva (las cargadas en Mi Perfil, o MAIL_ADMIN de respaldo). */
    private final NotificacionService notificacionService;

    public EmailServiceImpl(JavaMailSender mailSender, NotificacionService notificacionService) {
        this.mailSender = mailSender;
        this.notificacionService = notificacionService;
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

    /**
     * Aviso al admin de una consulta nueva. Corre en segundo plano (@Async, executor de Spring)
     * para no demorar la respuesta del formulario; si falla, solo se loguea.
     */
    @Override
    @Async
    public void notificarNuevaConsulta(String nombreCliente, String mailCliente, String telefonoCliente,
                                       String mensajeConsulta, String nombreMueble) {
        try {
            String[] destinatarios = notificacionService.destinatariosDelAviso();
            if (destinatarios.length == 0) {
                logger.warn("⚠️ [EMAIL] No hay casillas para avisar la consulta nueva (ni cargadas en Mi Perfil ni MAIL_ADMIN)");
                return;
            }
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setFrom(remitente);
            mensaje.setTo(destinatarios);
            mensaje.setReplyTo(mailCliente);
            mensaje.setSubject(nombreMueble != null
                    ? "MDZ Muebles - Consulta por " + nombreMueble
                    : "MDZ Muebles - Nueva consulta general");
            mensaje.setText(
                    "Entró una nueva consulta desde la web.\n\n" +
                    "Nombre: " + nombreCliente + "\n" +
                    "Email: " + mailCliente + "\n" +
                    "Teléfono: " + (telefonoCliente != null && !telefonoCliente.isBlank() ? telefonoCliente : "No indicado") + "\n" +
                    "Mueble: " + (nombreMueble != null ? nombreMueble : "Consulta general") + "\n\n" +
                    "Mensaje:\n" + mensajeConsulta
            );
            mailSender.send(mensaje);
            logger.info("✅ [EMAIL] Aviso de nueva consulta enviado");
        } catch (Exception e) {
            logger.error("❌ [EMAIL] Error al avisar la nueva consulta: {}", e.getMessage());
        }
    }

    @Override
    public void enviarPruebaNotificacion(String mailDestino) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(remitente);
        mensaje.setTo(mailDestino);
        mensaje.setSubject("MDZ Muebles - Mail de prueba");
        mensaje.setText("Este es un mail de prueba.\n\n"
                + "Si lo recibiste, esta casilla va a recibir el aviso de cada consulta nueva que llegue desde la web.\n\n"
                + "Equipo de MDZ Muebles");
        mailSender.send(mensaje);
        logger.info("✅ [EMAIL] Mail de prueba enviado a: {}", mailDestino);
    }
}
