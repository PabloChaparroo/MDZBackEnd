package com.Capinteria.carpinteria.Controller;

import com.Capinteria.carpinteria.Entity.MuebleImagenes;
import com.Capinteria.carpinteria.Entity.MuebleImagenesPortada;
import com.Capinteria.carpinteria.Service.MuebleImagenesPortadaService;
import com.Capinteria.carpinteria.Service.MuebleImagenesService;
import com.Capinteria.carpinteria.Service.MuebleImagenesServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "api/v1/muebleImagenes")
public class MuebleImagenesController extends BaseControllerImpl<MuebleImagenes, MuebleImagenesServiceImpl>{

    private MuebleImagenesService servicio;
    public MuebleImagenesController(MuebleImagenesService servicio) {
        this.servicio = servicio;
    }

    @PostMapping("/imagenes")
    public ResponseEntity<String> uploadImage(@RequestParam("files") List<MultipartFile> files) {
        try {
            // Aquí puedes procesar la carga de imágenes
            if (files != null && !files.isEmpty()) {
                for (MultipartFile file : files) {
                    byte[] bytes = file.getBytes();
                    MuebleImagenes imagenes = new MuebleImagenes();
                    imagenes.setImagenes(bytes);
                    servicio.save(imagenes);
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

    @PutMapping("/imagenes")
    public ResponseEntity<String> uploadImagesPut(@RequestParam("files") List<MultipartFile> files) {
        try {
            // Aquí puedes procesar la carga de imágenes
            if (files != null && !files.isEmpty()) {
                for (MultipartFile file : files) {
                    byte[] bytes = file.getBytes();
                    MuebleImagenes imagenes = new MuebleImagenes();
                    imagenes.setImagenes(bytes);
                    servicio.save(imagenes);
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
