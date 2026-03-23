package exceptions;

/**
 * EntidadNoEncontradaException — Se lanza cuando se busca un registro en la BD
 * por su ID y no existe (ej: Producto con id 99 no encontrado).
 * Hereda de BaseException (RuntimeException), no requiere throws.
 * El mensaje se genera automáticamente con el nombre de la entidad y el ID.
 */
public class EntidadNoEncontradaException extends BaseException {

    // Construye el mensaje "[Entidad] con id [X] no encontrado."
    // Ejemplo: new EntidadNoEncontradaException("Producto", 99)
    //          → mensaje: "Producto con id 99 no encontrado."
    public EntidadNoEncontradaException(String entidad, int id) {
        super(String.format("%s con id %d no encontrado.", entidad, id));
    }
}
