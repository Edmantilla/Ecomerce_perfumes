
package logica;

import exceptions.ValidacionException;
import java.math.BigDecimal;

/**
 *
 * @author eduar
 */
public class itemCarrito {

    private Producto   producto;
    private int        cantidad;
    private BigDecimal precioCapturado; // precio al momento de agregar

    public itemCarrito(Producto producto, int cantidad) {
        if (producto == null)
            throw new ValidacionException("El producto no puede ser null.");
        if (cantidad <= 0)
            throw new ValidacionException("La cantidad debe ser mayor a 0.");
        if (!producto.isDisponible())
            throw new ValidacionException("El producto '" + producto.getNombreProducto() + "' no está disponible.");
        this.producto        = producto;
        this.cantidad        = cantidad;
        this.precioCapturado = producto.getPrecio(); // precio fijo al agregar
    }

    /** Subtotal = precioCapturado × cantidad */
    public BigDecimal getSubtotal() {
        return precioCapturado.multiply(BigDecimal.valueOf(cantidad));
    }

    public void setCantidad(int cantidad) {
        if (cantidad <= 0) throw new ValidacionException("La cantidad debe ser mayor a 0.");
        this.cantidad = cantidad;
    }

    public Producto   getProducto()        { return producto; }
    public int        getCantidad()        { return cantidad; }
    public BigDecimal getPrecioCapturado() { return precioCapturado; }

    @Override public String toString() {
        return "ItemCarrito{producto='" + producto.getNombreProducto() + "'" +
               ", cantidad=" + cantidad + ", precioCapturado=" + precioCapturado +
               ", subtotal=" + getSubtotal() + '}';
    }
}    
