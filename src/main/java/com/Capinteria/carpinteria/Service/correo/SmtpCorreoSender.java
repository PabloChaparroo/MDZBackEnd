package com.Capinteria.carpinteria.Service.correo;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/** Envío por SMTP (spring.mail.*). Es el transporte por defecto: local va a Mailpit. */
@Component
@ConditionalOnProperty(name = "mdz.mail.proveedor", havingValue = "smtp", matchIfMissing = true)
public class SmtpCorreoSender implements CorreoSender {

    private final JavaMailSender mailSender;

    public SmtpCorreoSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void enviar(SimpleMailMessage mensaje) {
        mailSender.send(mensaje);
    }
}
