package com.Capinteria.carpinteria.Controller;

import com.Capinteria.carpinteria.Entity.Pedido;
import com.Capinteria.carpinteria.Service.PedidoServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "api/v1/pedido")
public class PedidoController extends BaseControllerImpl<Pedido, PedidoServiceImpl> {



}
