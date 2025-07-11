package com.Capinteria.carpinteria.Controller;

import com.Capinteria.carpinteria.Entity.MuebleImagenes;
import com.Capinteria.carpinteria.Service.MuebleImagenesService;
import com.Capinteria.carpinteria.Service.MuebleImagenesServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "api/v1/muebleImagenes")
public class MuebleImagenesController extends BaseControllerImpl<MuebleImagenes, MuebleImagenesServiceImpl>{

    private final MuebleImagenesService servicio;

    public MuebleImagenesController(MuebleImagenesService servicio) {
        this.servicio = servicio;
    }

    @PostMapping("/imagenes")
    public ResponseEntity<String> uploadImage(@RequestParam("files") List<MultipartFile> files) {
        return servicio.uploadImages(files);
    }

    @PutMapping("/imagenes")
    public ResponseEntity<String> uploadImagesPut(@RequestParam("files") List<MultipartFile> files) {
        return servicio.uploadImages(files);
    }
}
