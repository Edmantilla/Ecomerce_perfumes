package persistencias.exceptions;

/**
 * NonexistentEntityException — Excepción checked lanzada por los JpaControllers
 * cuando se intenta editar o eliminar una entidad que ya no existe en la BD.
 * 
 * Se lanza en los métodos edit() y destroy() de cada JpaController cuando:
 *   - em.merge() falla porque el registro fue eliminado entre la lectura y la escritura
 *   - em.getReference() no encuentra la entidad por su ID
 * 
 * Extiende Exception (checked), no RuntimeException, por lo que debe ser
 * capturada explícitamente con try/catch en los servlets que usan los controllers.
 */
public class NonexistentEntityException extends Exception {
    // Constructor con mensaje y causa original (encadena excepciones)
    // Usado en destroy() al capturar EntityNotFoundException
    public NonexistentEntityException(String message, Throwable cause) {
        super(message, cause);
    }
    // Constructor solo con mensaje descriptivo
    // Usado en edit() cuando findEntidad() retorna null
    public NonexistentEntityException(String message) {
        super(message);
    }
}
