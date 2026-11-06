package com.Capinteria.carpinteria.Service;

import com.Capinteria.carpinteria.Entity.DestinatarioNotificacion;
import com.Capinteria.carpinteria.Repositories.DestinatarioNotificacionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Casillas que reciben el aviso de "consulta nueva". Las administra el admin desde Mi Perfil.
 * Si no hay ninguna cargada se usa la de respaldo (mdz.mail.admin / variable MAIL_ADMIN),
 * así nunca se pierde un aviso por haber borrado todas.
 */
@Service
public class NotificacionService {

    /** Validación simple: algo@algo.dominio, sin espacios. */
    private static final Pattern MAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$");

    private final DestinatarioNotificacionRepository repository;
    private final String mailRespaldo;

    public NotificacionService(DestinatarioNotificacionRepository repository,
                               @Value("${mdz.mail.admin:}") String mailRespaldo) {
        this.repository = repository;
        this.mailRespaldo = mailRespaldo == null ? "" : mailRespaldo.trim();
    }

    public List<DestinatarioNotificacion> listar() {
        return repository.findAllByOrderByIdAsc();
    }

    public DestinatarioNotificacion agregar(String mail) {
        String limpio = mail == null ? "" : mail.trim().toLowerCase(Locale.ROOT);
        if (limpio.length() > 255 || !MAIL.matcher(limpio).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El mail no es válido");
        }
        if (repository.existsByMailIgnoreCase(limpio)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese mail ya está en la lista");
        }
        return repository.save(DestinatarioNotificacion.builder()
                .mail(limpio)
                .fechaAlta(LocalDateTime.now())
                .build());
    }

    public void quitar(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe ese destinatario");
        }
        repository.deleteById(id);
    }

    public DestinatarioNotificacion buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe ese destinatario"));
    }

    /** True si no hay casillas cargadas y los avisos van a la de respaldo. */
    public boolean usaRespaldo() {
        return repository.count() == 0;
    }

    /** Casillas a las que se manda el aviso: las cargadas o, si no hay ninguna, la de respaldo. */
    public String[] destinatariosDelAviso() {
        List<String> mails = listar().stream().map(DestinatarioNotificacion::getMail).toList();
        if (!mails.isEmpty()) {
            return mails.toArray(String[]::new);
        }
        return mailRespaldo.isEmpty() ? new String[0] : new String[]{mailRespaldo};
    }
}
