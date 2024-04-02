package com.Capinteria.carpinteria.Controller;

import com.Capinteria.carpinteria.Entity.Mueble;
import com.Capinteria.carpinteria.Entity.MuebleImagenes;
import com.Capinteria.carpinteria.Service.MuebleImagenesServiceImpl;
import com.Capinteria.carpinteria.Service.MuebleServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;



@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path="api/v1/mueble")
public class MuebleController extends BaseControllerImpl<Mueble, MuebleServiceImpl>{

    private static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024; // 10 MB
    private final MuebleServiceImpl muebleService;
    private final MuebleImagenesServiceImpl muebleImagenesService;
    private final ObjectMapper objectMapper;

    @Autowired
    public MuebleController(MuebleServiceImpl muebleService, MuebleImagenesServiceImpl muebleImagenesService, ObjectMapper objectMapper) {
        this.muebleService = muebleService;
        this.muebleImagenesService = muebleImagenesService;
        this.objectMapper =  objectMapper;
    }

    @PostMapping(value = "/create", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<String> create(@RequestParam("mueble") String muebleJson, @RequestParam("files") List<MultipartFile> files) {
        try {
            // Deserializar el JSON a un objeto Mueble
            Mueble mueble = objectMapper.readValue(muebleJson, Mueble.class);

            // Agregar mensajes de registro para los datos recibidos del frontend
            System.out.println("Datos recibidos del frontend - Mueble: " + mueble);
            System.out.println("Archivos recibidos del frontend: " + files);

            // Validar si se ha enviado el objeto Mueble
            if (mueble == null) {
                return ResponseEntity.badRequest().body("El objeto Mueble no puede ser nulo");
            }

            // Validar si se han enviado archivos
            if (files == null || files.isEmpty()) {
                return ResponseEntity.badRequest().body("Debe enviar al menos un archivo");
            }

            // Guardar el mueble
            Mueble savedMueble = muebleService.save(mueble);

            // Asociar las imágenes con el mueble
            for (MultipartFile file : files) {
                // Validar el tamaño del archivo
                if (file.getSize() > MAX_FILE_SIZE_BYTES) {
                    return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body("El tamaño del archivo excede el límite permitido");
                }

                byte[] bytes = file.getBytes();
                MuebleImagenes imagenes = new MuebleImagenes();
                imagenes.setImagenes(bytes);
                savedMueble.getImagenes().add(imagenes);
                muebleImagenesService.save(imagenes);
            }

            return ResponseEntity.ok("Mueble y imágenes asociadas creadas correctamente");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al cargar las imágenes: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al procesar la solicitud: " + e.getMessage());
        }
    }



    @PutMapping("/{id}/imagenes")
    public ResponseEntity<?> updateImages(@PathVariable Long id, @RequestParam("files") MultipartFile[] files) {
        try {
            // Obtener el mueble por su ID
            Mueble mueble = muebleService.findById(id);

            // Verificar si el mueble existe
            if (mueble != null) {
                // Limpiar las imágenes anteriores del mueble
                mueble.getImagenes().clear();

                // Asociar las nuevas imágenes con el mueble
                for (MultipartFile file : files) {
                    byte[] bytes = file.getBytes();
                    MuebleImagenes imagenes = new MuebleImagenes();
                    imagenes.setImagenes(bytes);
                    mueble.getImagenes().add(imagenes);
                }

                // Guardar el mueble actualizado
                muebleService.save(mueble);

                return ResponseEntity.ok("Imágenes actualizadas correctamente");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No se encontró el mueble con el ID proporcionado");
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al procesar las imágenes: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al actualizar las imágenes del mueble: " + e.getMessage());
        }
    }
/*
    @PostMapping(value = "/create", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<String> create(
            @RequestParam("mueble") String muebleJson,
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "portadaIndex", required = false) Integer portadaIndex) {
        try {
            // Deserializar el JSON a un objeto Mueble
            Mueble mueble = objectMapper.readValue(muebleJson, Mueble.class);

            // Agregar mensajes de registro para los datos recibidos del frontend
            System.out.println("Datos recibidos del frontend - Mueble: " + mueble);
            System.out.println("Archivos recibidos del frontend: " + files);
            System.out.println("Índice de la imagen de portada: " + portadaIndex);

            // Tu lógica para guardar el mueble y las imágenes, incluida la imagen de portada

            return ResponseEntity.ok("Mueble y imágenes asociadas y creadas correctamente");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al cargar las imágenes: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al procesar la solicitud: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/imagenes")
    public ResponseEntity<?> updateImages(
            @PathVariable Long id,
            @RequestParam(value = "files", required = false) MultipartFile[] files,
            @RequestParam(value = "portadaIndex", required = false) Integer portadaIndex) {
        try {
            // Obtener el mueble por su ID
            Mueble mueble = muebleService.findById(id);

            // Verificar si el mueble existe
            if (mueble != null) {
                // Limpiar las imágenes anteriores del mueble
                mueble.getImagenes().clear();

                // Asociar las nuevas imágenes con el mueble
                if (files != null && files.length > 0) {
                    for (int i = 0; i < files.length; i++) {
                        byte[] bytes = files[i].getBytes();
                        MuebleImagenes imagenes = new MuebleImagenes();
                        imagenes.setImagenes(bytes);
                        if (portadaIndex != null && i == portadaIndex) {
                            imagenes.setEsPortada(true); // Marcar esta imagen como portada si corresponde
                        }
                        mueble.getImagenes().add(imagenes);
                    }
                }

                // Guardar el mueble actualizado
                muebleService.save(mueble);

                return ResponseEntity.ok("Imágenes actualizadas correctamente");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No se encontró el mueble con el ID proporcionado");
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al procesar las imágenes: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al actualizar las imágenes del mueble: " + e.getMessage());
        }
    }
*/



    @GetMapping("/page/{pageNumber}")
    public ResponseEntity<Page<Mueble>> getMueblesByPage(@PathVariable int pageNumber) {
        try {
            // Configurar la paginación con 9 elementos por página
            Pageable pageable = PageRequest.of(pageNumber, 9);
            // Obtener la página de muebles
            Page<Mueble> mueblesPage = muebleService.findAll(pageable);
            // Devolver la página de muebles
            return ResponseEntity.ok(mueblesPage);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

}
