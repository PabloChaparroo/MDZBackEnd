package com.Capinteria.carpinteria.Controller;

import com.Capinteria.carpinteria.DTO.ClienteDTO;
import com.Capinteria.carpinteria.DTO.ClienteModifyDTO;
import com.Capinteria.carpinteria.Entity.Cliente;
import com.Capinteria.carpinteria.Service.ClienteServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "api/v1/cliente")
public class ClienteController extends BaseControllerImpl<Cliente, ClienteServiceImpl> {

    private static final Logger logger = LoggerFactory.getLogger(ClienteController.class);

    @PostMapping("/crear")
    public ResponseEntity<?> crearCliente(@RequestBody Cliente cliente) {
        logger.info("[API-CREAR-CLIENTE] Recibida solicitud de creación de cliente: {} {}", 
                   cliente != null ? cliente.getNombreCliente() : "null",
                   cliente != null ? cliente.getApellidoCliente() : "null");
        
        try {
            Cliente clienteCreado = servicio.crearCliente(cliente);
            
            // Formatear la fecha de alta para el frontend
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String fechaAltaFormateada = clienteCreado.getFechaHoraAltaCliente().format(formatter);
            
            logger.info("[API-CREAR-CLIENTE] Cliente creado exitosamente con ID: {}", clienteCreado.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                .body("{\"mensaje\":\"Cliente creado exitosamente\"," +
                      "\"clienteId\":" + clienteCreado.getId() + "," +
                      "\"nombre\":\"" + clienteCreado.getNombreCliente() + "\"," +
                      "\"apellido\":\"" + clienteCreado.getApellidoCliente() + "\"," +
                      "\"email\":\"" + clienteCreado.getMailCliente() + "\"," +
                      "\"telefono\":" + clienteCreado.getTelefonoCliente() + "," +
                      "\"fechaAlta\":\"" + fechaAltaFormateada + "\"}");
            
        } catch (RuntimeException e) {
            logger.error("[API-CREAR-CLIENTE] Error al crear cliente: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("[API-CREAR-CLIENTE] Error interno al crear cliente: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\":\"Error interno del servidor\"}");
        }
    }

    @GetMapping("/searchById")
    public ResponseEntity<?> searchById(Long idCliente) {
        try {
            Cliente cliente = servicio.searchById(idCliente);
            return ResponseEntity.status(HttpStatus.OK).body(cliente);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(("{\"error\": \"" + e.getMessage() + "\"}"));
        }
    }

    @GetMapping("/showProfile")
    public ResponseEntity<?> showProfile(@RequestHeader(name = "Authorization") String token) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(servicio.showProfile(token));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(("{\"error\": \"" + e.getMessage() + "\"}"));
        }
    }

    @PutMapping("/updateProfile")
    public ResponseEntity<?> updateProfile(@RequestHeader(name = "Authorization") String token, @RequestBody ClienteDTO clienteActualizado) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(servicio.updateProfile(token, clienteActualizado));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"Los datos de la solicitud son inválidos. Por favor, revise los datos e intente de nuevo.\"}");
        }
    }

    @PutMapping("/modifyCliente")
    public ResponseEntity<?> modifyCliente(@RequestBody ClienteModifyDTO clienteModifyDTO) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(servicio.modifyCliente(clienteModifyDTO));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"Los datos de la solicitud son inválidos. Por favor, revise los datos e intente de nuevo.\"}");
        }
    }

    @PutMapping("/deleteCliente")
    public ResponseEntity<?> deleteCliente(@RequestBody Long idCliente) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(servicio.deleteCliente(idCliente));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"Los datos de la solicitud son inválidos. Por favor, revise los datos e intente de nuevo.\"}");
        }
    }

    @GetMapping("/byMailCliente")
    public ResponseEntity<?> getClienteByMailCliente(@RequestParam String mailCliente) {
        try {
            Cliente cliente = servicio.getClienteByMailCliente(mailCliente);
            return ResponseEntity.status(HttpStatus.OK).body(cliente);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(("{\"error\": \"" + e.getMessage() + "\"}"));
        }
    }



}