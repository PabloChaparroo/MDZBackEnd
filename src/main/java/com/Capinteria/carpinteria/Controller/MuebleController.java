package com.Capinteria.carpinteria.Controller;

import com.Capinteria.carpinteria.Entity.Mueble;
import com.Capinteria.carpinteria.Entity.MuebleImagenes;
import com.Capinteria.carpinteria.Service.MuebleImagenesServiceImpl;
import com.Capinteria.carpinteria.Service.MuebleServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.lang.model.util.Elements;
import java.io.IOException;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path="api/v1/mueble")
public class MuebleController extends BaseControllerImpl<Mueble, MuebleServiceImpl>{
    private final MuebleServiceImpl muebleService;
    private final MuebleImagenesServiceImpl muebleImagenesService;

    @Autowired
    public MuebleController(MuebleServiceImpl muebleService, MuebleImagenesServiceImpl muebleImagenesService) {
        this.muebleService = muebleService;
        this.muebleImagenesService = muebleImagenesService;
    }

    @PostMapping("/create")
    public ResponseEntity<String> create(@RequestBody Mueble mueble, @RequestParam("files") List<MultipartFile> files) {
        try {
            // Guardar el mueble
            Mueble savedMueble = muebleService.save(mueble);

            // Asociar las imágenes con el mueble
            if (files != null && !files.isEmpty()) {
                for (MultipartFile file : files) {
                    byte[] bytes = file.getBytes();
                    MuebleImagenes imagenes = new MuebleImagenes();

                    imagenes.setImagenes(bytes);
                    savedMueble.getImagenes().add(imagenes);

                    muebleImagenesService.save(imagenes);
                }
            }

            return ResponseEntity.ok("Mueble y imágenes asociadas creadas correctamente");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al cargar las imágenes");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al cargar las imágenes");
        }
    }


}
