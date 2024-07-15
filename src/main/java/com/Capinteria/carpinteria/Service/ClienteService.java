package com.Capinteria.carpinteria.Service;

import com.Capinteria.carpinteria.DTO.ClienteDTO;
import com.Capinteria.carpinteria.DTO.ClienteModifyDTO;
import com.Capinteria.carpinteria.Entity.Cliente;

public interface ClienteService extends BaseService<Cliente,Long> {
    Cliente searchById(Long idCliente) throws Exception;

    ClienteDTO showProfile(String token) throws Exception;

    ClienteDTO updateProfile(String token, ClienteDTO clienteActualizado) throws Exception;

    Cliente modifyCliente(ClienteModifyDTO clienteModifyDTO) throws Exception;

    Cliente deleteCliente(Long idCliente) throws Exception;

    Cliente getClienteByMailCliente(String mailCliente) throws Exception;




}
