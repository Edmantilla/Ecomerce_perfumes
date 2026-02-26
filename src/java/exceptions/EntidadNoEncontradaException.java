package exceptions;

public class EntidadNoEncontradaException extends BaseException {

    public EntidadNoEncontradaException(String entidad, int id) {
        super(String.format("%s con id %d no encontrado.", entidad, id));
    }
}
