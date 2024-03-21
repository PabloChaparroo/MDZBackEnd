package com.Capinteria.carpinteria.Controller;

import com.Capinteria.carpinteria.Entity.Usuario;
import com.Capinteria.carpinteria.Service.UsuarioServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "api/v1/usuario")
public class UsuarioController extends BaseControllerImpl<Usuario, UsuarioServiceImpl> {

    @GetMapping("/findUserByUsername")
    public ResponseEntity<?> findUserByUsername(@RequestParam String username) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(servicio.findUserByUsername(username));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(("{\"error\": \"" + e.getMessage() + "\"}"));
        }
    }
}

