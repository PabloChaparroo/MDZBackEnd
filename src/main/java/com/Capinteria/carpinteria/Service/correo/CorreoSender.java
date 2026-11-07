package com.Capinteria.carpinteria.Service.correo;

import org.springframework.mail.SimpleMailMessage;

/**
 * Transporte que efectivamente entrega un mail. Se elige con mdz.mail.proveedor (variable MAIL_PROVIDER):
 * <ul>
 *   <li>{@code smtp} (por defecto): JavaMailSender. Local con Mailpit, tests.</li>
 *   <li>{@code gmail-api}: API HTTP de Gmail. Producción en Render, donde el plan gratis bloquea los puertos SMTP.</li>
 * </ul>
 * Si el envío falla, lanza una excepción (la respuesta a una consulta no se guarda si el mail no salió).
 */
public interface CorreoSender {

    void enviar(SimpleMailMessage mensaje);
}
