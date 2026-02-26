package exceptions;

public class BaseException extends RuntimeException {

    public BaseException(String mensaje) {
        super(mensaje);
    }

    public BaseException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
