package exceptions;

/**
 * ValidacionException — Se lanza cuando los datos proporcionados no cumplen
 * las reglas de negocio (ej: nombre vacío, precio negativo, formato inválido).
 * Hereda de BaseException (RuntimeException), no requiere throws.
 * Se usa en las entidades JPA y en los servlets para validar inputs.
 */
public class ValidacionException extends BaseException {

    // Constructor que recibe un mensaje descriptivo del error de validación
    // Ejemplo: new ValidacionException("El nombre del producto no puede estar vacío")
    public ValidacionException(String mensaje) {
        super(mensaje);
    }
}
