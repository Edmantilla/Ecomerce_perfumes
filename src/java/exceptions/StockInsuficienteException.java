package exceptions;

public class StockInsuficienteException extends BaseException {

    public StockInsuficienteException(String nombreProducto, int disponible, int solicitado) {
        super(String.format("Stock insuficiente para '%s'. Disponible: '%d',Solicitado: %d", nombreProducto, disponible, solicitado));
    }
}
