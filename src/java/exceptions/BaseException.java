package exceptions;

/**
 * BaseException — Excepción base del proyecto.
 * Extiende RuntimeException (excepción no-chequeada), por lo que no requiere
 * declaración throws en los métodos que la lanzan.
 * Todas las excepciones personalizadas del proyecto heredan de esta clase:
 *   - ValidacionException (datos inválidos)
 *   - EntidadNoEncontradaException (registro no existe en BD)
 *   - StockInsuficienteException (no hay suficientes unidades)
 */
public class BaseException extends RuntimeException {

    // Constructor con solo mensaje de error
    public BaseException(String mensaje) {
        super(mensaje); // pasa el mensaje a RuntimeException
    }

    // Constructor con mensaje de error y causa original (excepción anidada)
    public BaseException(String mensaje, Throwable causa) {
        super(mensaje, causa); // pasa ambos a RuntimeException
    }
}
