package com.Capinteria.carpinteria.Controller;

import com.Capinteria.carpinteria.Entity.Categoria;
import com.Capinteria.carpinteria.Service.CategoriaServiceImpl;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "api/v1/categoria")
public class CategoriaController extends BaseControllerImpl<Categoria, CategoriaServiceImpl> {
}
