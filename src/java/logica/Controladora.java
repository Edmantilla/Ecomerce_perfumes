
package logica;

import persistencias.ControladoraPersistencia; // importa la capa de persistencia general

/**
 * Controladora — Clase puente entre la lógica de negocio y la capa de persistencia.
 *
 * Actualmente es una clase vacía que solo instancia ControladoraPersistencia.
 * Fue diseñada como punto central de acceso a la BD en una arquitectura de capas
 * tradicional (antes de que se adoptaran los servlets directamente con JPA).
 * No se usa activamente en la versión actual del proyecto.
 */
public class Controladora {
    
    // Instancia de la capa de persistencia general del proyecto
    // Permite a esta clase (y a sus posibles subclases) hacer operaciones en BD
    ControladoraPersistencia controlPersis = new ControladoraPersistencia();
   
}
