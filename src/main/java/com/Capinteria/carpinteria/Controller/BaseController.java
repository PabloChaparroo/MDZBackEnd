package com.Capinteria.carpinteria.Controller;

import com.Capinteria.carpinteria.Entity.BaseEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

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



