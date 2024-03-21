package com.Capinteria.carpinteria.Service;

import com.Capinteria.carpinteria.Entity.Domicilio;

public interface DomicilioService extends BaseService<Domicilio,Long> {
    boolean domicilioPerteneceAlCliente(Long idDomicilio, Long idCliente) throws Exception;
}
