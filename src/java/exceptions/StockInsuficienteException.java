package exceptions;

/**
 * StockInsuficienteException — Se lanza cuando se intenta comprar más unidades
 * de un producto de las que hay disponibles en inventario.
 * Hereda de BaseException (RuntimeException), no requiere throws.
 * Se usa principalmente en el flujo de compra (SvCompra / itemCarrito).
 */
public class StockInsuficienteException extends BaseException {

    // Construye el mensaje indicando qué producto, cuánto hay y cuánto se pidió
    // Ejemplo: new StockInsuficienteException("Sauvage EDP", 5, 10)
    //          → mensaje: "Stock insuficiente para 'Sauvage EDP'. Disponible: '5', Solicitado: 10"
    public StockInsuficienteException(String nombreProducto, int disponible, int solicitado) {
        super(String.format("Stock insuficiente para '%s'. Disponible: '%d',Solicitado: %d", nombreProducto, disponible, solicitado));
    }
}
