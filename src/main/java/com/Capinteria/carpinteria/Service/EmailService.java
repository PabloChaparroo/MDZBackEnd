package com.Capinteria.carpinteria.Service;

public interface EmailService {
    /**
     * Envía por mail la respuesta del administrador a la consulta de un cliente.
     */
    void enviarRespuestaConsulta(String mailDestino, String nombreCliente, String mensajeConsulta, String respuesta);
}
