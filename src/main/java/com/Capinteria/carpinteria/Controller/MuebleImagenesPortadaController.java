package com.Capinteria.carpinteria.Controller;

import com.Capinteria.carpinteria.Entity.MuebleImagenesPortada;
import com.Capinteria.carpinteria.Service.MuebleImagenesPortadaService;
import com.Capinteria.carpinteria.Service.MuebleImagenesPortadaServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "api/v1/muebleImagenesPortada")
public class MuebleImagenesPortadaController extends BaseControllerImpl<MuebleImagenesPortada, MuebleImagenesPortadaServiceImpl> {
    private MuebleImagenesPortadaService servicio;


    public MuebleImagenesPortadaController(MuebleImagenesPortadaService servicio) {
        this.servicio = servicio;
    }

    @PostMapping("/imagenesPortada")
    public ResponseEntity<String> uploadImages(@RequestParam("files") List<MultipartFile> files) {
        try {
            // Aquí puedes procesar la carga de imágenes
            if (files != null && !files.isEmpty()) {
                for (MultipartFile file : files) {
                    byte[] bytes = file.getBytes();
                    MuebleImagenesPortada imagen = new MuebleImagenesPortada();
                    imagen.setImagenPortada(bytes);
                    servicio.save(imagen);
                }

                return ResponseEntity.ok("Imágenes cargadas correctamente");
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al cargar las imágenes");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al cargar las imágenes");
        }
        return ResponseEntity.badRequest().body("No se han proporcionado imágenes");
    }
}
