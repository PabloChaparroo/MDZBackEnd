package com.Capinteria.carpinteria.Service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * El aviso de consulta nueva se manda en segundo plano con el executor de Spring
 * (no en el ForkJoinPool común, donde Jakarta Mail falla cuando la app corre como jar).
 */
@SpringBootTest
class EmailServiceAsyncTest {

    @Autowired
    private EmailService emailService;

    @MockBean
    private JavaMailSender mailSender;

    @Test
    void elAvisoDeConsultaNuevaSeMandaEnOtroHiloConElClassloaderDeLaApp() throws Exception {
        CompletableFuture<SimpleMailMessage> enviado = new CompletableFuture<>();
        CompletableFuture<Thread> hilo = new CompletableFuture<>();
        doAnswer(inv -> {
            hilo.complete(Thread.currentThread());
            enviado.complete(inv.getArgument(0));
            return null;
        }).when(mailSender).send(any(SimpleMailMessage.class));

        emailService.notificarNuevaConsulta("Ana Paz", "ana@cliente.test", null, "Precio?", "Mesa ratona");

        SimpleMailMessage mensaje = enviado.get(10, TimeUnit.SECONDS);
        assertThat(mensaje.getSubject()).isEqualTo("MDZ Muebles - Consulta por Mesa ratona");
        assertThat(mensaje.getReplyTo()).isEqualTo("ana@cliente.test");

        Thread t = hilo.get();
        assertThat(t).isNotSameAs(Thread.currentThread());
        assertThat(t.getName()).as("hilo del executor de Spring, no del ForkJoinPool común")
                .doesNotContain("ForkJoinPool");
        assertThat(t.getContextClassLoader()).isSameAs(EmailServiceImpl.class.getClassLoader());
    }

    @Test
    void siElSmtpFallaElAvisoNoPropagaElError() {
        doAnswer(inv -> { throw new RuntimeException("SMTP caído"); }).when(mailSender).send(any(SimpleMailMessage.class));

        // No tiene que lanzar: el formulario del cliente no puede fallar por el aviso
        emailService.notificarNuevaConsulta("Ana", "ana@cliente.test", "", "Hola", null);
    }
}
