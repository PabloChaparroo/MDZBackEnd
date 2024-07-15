package com.Capinteria.carpinteria;

import com.Capinteria.carpinteria.Entity.Cliente;
import com.Capinteria.carpinteria.Entity.Usuario;
import com.Capinteria.carpinteria.Repositories.ClienteRepository;
import com.Capinteria.carpinteria.Repositories.UsuarioRepository;
import com.Capinteria.carpinteria.enumeration.EstadoCliente;
import com.Capinteria.carpinteria.enumeration.Role;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.LocalDate;

@SpringBootApplication
public class CarpinteriaApplication {


	@Autowired
	private ClienteRepository clienteRepository;

	@Autowired
	private UsuarioRepository usuarioRepository;

	public CarpinteriaApplication(ClienteRepository clienteRepository) {
		this.clienteRepository = clienteRepository;
	}

	public static void main(String[] args) {
		SpringApplication.run(CarpinteriaApplication.class, args);
		System.out.println("--------------------ESTÁ FUNCIONANDO CORRECTAMENTE------------------------");

	}

	@PostConstruct
	private void crearAdministrador(){
		Cliente cliente1 = Cliente.builder()
				.nombreCliente("Estaban")
				.apellidoCliente("Chaparro")
				.telefonoCliente(261441926)
				.mailCliente("esteban@gmail.com")
				.fechaHoraAltaCliente(LocalDate.now())
				.estadoCliente(EstadoCliente.ALTA)
				.build();

		Usuario user = Usuario.builder()
				.username("esteban@gmail.com")
				.password("123456")
				.fechaAltaUsuario(LocalDate.now())
				.role(Role.ADMIN) //ver numeracion de roles
				.build();

		user.setCliente(cliente1);
		usuarioRepository.save(user);
	}


}
