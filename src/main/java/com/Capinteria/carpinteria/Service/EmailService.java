package com.Capinteria.carpinteria.Service;

public interface EmailService {
    /**
     * Envía por mail la respuesta del administrador a la consulta de un cliente.
     */
    void enviarRespuestaConsulta(String mailDestino, String nombreCliente, String mensajeConsulta, String respuesta);

    /**
     * Avisa al administrador que entró una consulta. Se envía en segundo plano y un
     * fallo solo se registra en el log: la consulta ya quedó guardada en la base.
     *
     * @param nombreMueble mueble consultado, o null si es una consulta general
     */
    void notificarNuevaConsulta(String nombreCliente, String mailCliente, String telefonoCliente,
                                String mensajeConsulta, String nombreMueble);

    /**
     * Manda un mail de prueba a una casilla de notificaciones. Es síncrono: si el envío
     * falla, lanza la excepción para que el panel pueda avisarlo.
     */
    void enviarPruebaNotificacion(String mailDestino);
}
