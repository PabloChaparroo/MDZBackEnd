package com.Capinteria.carpinteria.Service;

import com.Capinteria.carpinteria.Entity.MuebleImagenes;
import com.Capinteria.carpinteria.Repositories.MuebleImagenesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class MuebleImagenesServiceImpl extends BaseSeriviceImpl<MuebleImagenes, Long> implements MuebleImagenesService {

    private final MuebleImagenesRepository muebleImagenesRepository;

    @Autowired
    public MuebleImagenesServiceImpl(MuebleImagenesRepository muebleImagenesRepository) {
        super(muebleImagenesRepository);
        this.muebleImagenesRepository = muebleImagenesRepository;
    }

    public ResponseEntity<String> uploadImages(List<MultipartFile> files) {
        try {
            if (files != null && !files.isEmpty()) {
                for (MultipartFile file : files) {
                    byte[] bytes = file.getBytes();
                    MuebleImagenes imagenes = new MuebleImagenes();
                    imagenes.setImagenes(bytes);
                    muebleImagenesRepository.save(imagenes);
                }
                return ResponseEntity.ok("Imágenes cargadas correctamente");
            }
            return ResponseEntity.badRequest().body("No se han proporcionado imágenes");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al cargar las imágenes");
        }
    }
}
