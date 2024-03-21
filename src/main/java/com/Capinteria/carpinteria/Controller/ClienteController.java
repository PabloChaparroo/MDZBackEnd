package com.Capinteria.carpinteria.Controller;

import com.Capinteria.carpinteria.DTO.ClienteDTO;
import com.Capinteria.carpinteria.DTO.ClienteModifyDTO;
import com.Capinteria.carpinteria.Entity.Cliente;
import com.Capinteria.carpinteria.Service.ClienteServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "api/v1/cliente")
public class ClienteController extends BaseControllerImpl<Cliente, ClienteServiceImpl> {

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


}