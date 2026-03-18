
package logica;

import exceptions.ValidacionException; // excepción de negocio para validaciones
import java.math.BigDecimal;           // para cálculos de precio sin errores de redondeo

/**
 * itemCarrito — Clase de lógica de negocio que representa un ítem dentro del carrito de compras.
 *
 * IMPORTANTE: Esta clase NO es una entidad JPA (no tiene @Entity ni mapea una tabla).
 * Es un objeto temporal de memoria usado únicamente durante el proceso de checkout en SvCompra.
 * El carrito del lado del cliente (browser) se almacena en localStorage como JSON.
 * Al hacer checkout, SvCompra recibe ese JSON y puede crear instancias de esta clase
 * para validar disponibilidad y calcular totales antes de crear el Pedido y sus Detallepedido.
 *
 * Al crear un itemCarrito, el precio del producto se captura en ese momento (precioCapturado),
 * garantizando que el precio no cambie aunque el producto se actualice luego.
 */
public class itemCarrito {

    private Producto   producto;         // referencia al producto comprado
    private int        cantidad;         // cuántas unidades se van a comprar
    private BigDecimal precioCapturado;  // precio al momento de agregar al carrito (se congela)

    // Constructor: crea un ítem de carrito con validaciones de negocio
    public itemCarrito(Producto producto, int cantidad) {
        if (producto == null)                               // no se puede agregar un producto nulo
            throw new ValidacionException("El producto no puede ser null.");
        if (cantidad <= 0)                                  // la cantidad debe ser positiva
            throw new ValidacionException("La cantidad debe ser mayor a 0.");
        if (!producto.isDisponible())                       // verificar que el producto tenga stock y esté activo
            throw new ValidacionException("El producto '" + producto.getNombreProducto() + "' no está disponible.");
        this.producto        = producto;
        this.cantidad        = cantidad;
        this.precioCapturado = producto.getPrecio();        // congela el precio actual del producto
        // Si el precio del producto sube después, el carrito mantiene el precio original
    }

    // Calcula el subtotal de este ítem: precioCapturado × cantidad
    // Ejemplo: 3 unidades a $180.000 = $540.000
    public BigDecimal getSubtotal() {
        return precioCapturado.multiply(BigDecimal.valueOf(cantidad)); // multiply es seguro para BigDecimal
    }

    // Actualiza la cantidad con validación: no puede ser 0 ni negativa
    // Llamado cuando el usuario cambia la cantidad en el carrito del navegador
    public void setCantidad(int cantidad) {
        if (cantidad <= 0) throw new ValidacionException("La cantidad debe ser mayor a 0."); // validación de negocio
        this.cantidad = cantidad;
    }

    public Producto   getProducto()        { return producto; }        // retorna el producto de este ítem
    public int        getCantidad()        { return cantidad; }        // retorna las unidades a comprar
    public BigDecimal getPrecioCapturado() { return precioCapturado; } // retorna el precio congelado al agregar

    // Representación en texto para logs: muestra producto, cantidad, precio congelado y subtotal
    @Override public String toString() {
        return "ItemCarrito{producto='" + producto.getNombreProducto() + "'" +
               ", cantidad=" + cantidad + ", precioCapturado=" + precioCapturado +
               ", subtotal=" + getSubtotal() + '}';
    }
}    
