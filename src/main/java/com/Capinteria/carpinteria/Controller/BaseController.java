package com.Capinteria.carpinteria.Controller;

import com.Capinteria.carpinteria.Entity.BaseEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.awt.print.Pageable;
import java.io.Serializable;
import java.util.List;

public interface BaseController <E extends BaseEntity, ID extends Serializable> {

    public ResponseEntity<?> getAll();
/*
    public ResponseEntity<?> getAll(Pageable pageable);*/

    public ResponseEntity<?> getOne(@PathVariable ID id);

    public ResponseEntity<?> save(@RequestBody E entity);

    public ResponseEntity<?> saveAll(@RequestBody List<E> entity);

    public ResponseEntity<?> update(@PathVariable ID id,@RequestBody E entity);

    public ResponseEntity<?> delete(@PathVariable ID id);
}
    //@PathVariable extrae valores de la URL.
    //@RequestBody convierte datos del cuerpo de la solicitud en un objeto Java.
    //@PathVariable antes de un parámetro del método en un controlador de Spring,
    //le indicas a Spring que debe extraer ese valor de la URL.

    /* @RequestBody Esta anotación se utiliza para indicar que un parámetro del método debe extraerse
    del cuerpo de la solicitud HTTP. Cuando enviamos datos en el cuerpo de una solicitud
    (por ejemplo, en formato JSON o XML), @RequestBody se utiliza para convertir esos datos en un objeto Java.*/


