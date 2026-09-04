package com.Capinteria.carpinteria;

import com.Capinteria.carpinteria.Entity.Cliente;
import com.Capinteria.carpinteria.Entity.Usuario;
import com.Capinteria.carpinteria.Repositories.UsuarioRepository;
import com.Capinteria.carpinteria.enumeration.Role;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.Capinteria.carpinteria.Repositories")
@EntityScan(basePackages = "com.Capinteria.carpinteria.Entity")
public class CarpinteriaApplication {

	private static final Logger logger = LoggerFactory.getLogger(CarpinteriaApplication.class);

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	public static void main(String[] args) {
		SpringApplication.run(CarpinteriaApplication.class, args);
		logger.info(">>> APLICACION INICIADA CORRECTAMENTE - MDZ MUEBLES BACKEND <<<");
	}

	@PostConstruct
	private void crearAdministrador(){
		logger.info("[SETUP] Verificando usuario administrador por defecto...");
		
		// Verificar si el usuario ya existe
		String adminUsername = "esteban@gmail.com";
		Optional<Usuario> usuarioExistente = usuarioRepository.findByUsername(adminUsername);
		if (usuarioExistente.isPresent()) {
			logger.info("[SETUP] Usuario administrador ya existe: {}", adminUsername);
			return;
		}
		
		logger.info("[SETUP] Creando usuario administrador por defecto...");
		
		Cliente cliente1 = Cliente.builder()
				.nombreCliente("Esteban")
				.apellidoCliente("Chaparro")
				.telefonoCliente(261441926)
				.mailCliente("esteban@gmail.com")
				.fechaHoraAltaCliente(java.time.LocalDateTime.now())
				.build();

		Usuario user = Usuario.builder()
				.username("esteban@gmail.com")
				.password(passwordEncoder.encode("123456")) // ENCRIPTAR la contraseña
				.fechaAltaUsuario(LocalDate.now())
				.role(Role.ADMIN)
				.build();

		user.setCliente(cliente1);
		usuarioRepository.save(user);
		
		logger.info("[OK] Usuario administrador creado exitosamente: {}", user.getUsername());
	}


}
