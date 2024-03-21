package com.Capinteria.carpinteria.Controller;

import com.Capinteria.carpinteria.Entity.Mueble;
import com.Capinteria.carpinteria.Service.MuebleServiceImpl;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.lang.model.util.Elements;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path="api/v1/mueble")
public class MuebleController extends BaseControllerImpl<Mueble, MuebleServiceImpl>{


}
