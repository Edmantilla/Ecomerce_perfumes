
package logica;

import exceptions.ValidacionException; // excepción de negocio para validaciones
import java.math.BigDecimal;           // para manejar precios con precisión decimal exacta
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Detallepedido — Entidad JPA que mapea la tabla "detalle_pedido".
 *
 * Representa una línea de un pedido: qué producto se compró, cuántos y a qué precio.
 * Un pedido tiene muchas líneas de detalle (relación N:1 con Pedido).
 * El precio se guarda en el momento de la compra para que no cambie si el producto sube de precio.
 * Se crea en SvCompra.java durante el proceso de checkout del carrito.
 */
@Entity                           // esta clase es una entidad JPA (mapea una tabla)
@Table(name = "detalle_pedido")   // nombre de la tabla en MySQL
public class Detallepedido {

    @Id                                                    // clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY)    // AUTO_INCREMENT en MySQL
    @Column(name = "id_detalle")                           // columna id_detalle en la tabla
    private int idDetalle;

    @ManyToOne                                             // relación N:1 — muchos detalles pertenecen a un pedido
    @JoinColumn(name = "id_pedido", nullable = false)      // FK al pedido padre, no puede ser nula
    private Pedido pedido;

    @ManyToOne                                             // relación N:1 — muchos detalles pueden referenciar un producto
    @JoinColumn(name = "id_producto", nullable = false)    // FK al producto comprado, no puede ser nula
    private Producto producto;

    @Column(name = "cantidad", nullable = false)           // número de unidades compradas
    private int cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2) // precio por unidad al momento de la compra
    // Se usa BigDecimal para evitar errores de redondeo con double/float en cálculos de dinero
    private BigDecimal precioUnitario;

    @Column(name = "activo", nullable = false)             // permite ocultar un ítem sin eliminarlo físicamente
    private boolean activo;

    public Detallepedido() {
    } // constructor vacío requerido por JPA

    // Constructor completo para crear un detalle con todos sus datos
    public Detallepedido(int idDetalle, Pedido pedido, Producto producto, int cantidad, BigDecimal precioUnitario, boolean activo) {
        this.idDetalle = idDetalle;
        this.pedido = pedido;
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.activo = activo;
    }
    
    // Método de negocio: calcula el subtotal de esta línea (precio × cantidad)
    // Si precioUnitario es null (dato corrupto), retorna 0 para no lanzar NullPointerException
    // Ejemplo: 2 unidades a $250.000 = $500.000
    public BigDecimal getSubtotal() {
        if (precioUnitario == null) return BigDecimal.ZERO; // protección contra datos nulos
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad)); // precio × cantidad
    }

    // Retorna el ID del detalle (clave primaria)
    public int getIdDetalle() {
        return idDetalle;
    }

    // Asigna el ID (normalmente solo lo usa JPA)
    public void setIdDetalle(int idDetalle) {
        this.idDetalle = idDetalle;
    }

    // Retorna el pedido al que pertenece esta línea
    public Pedido getPedido() {
        return pedido;
    }

    // Vincula esta línea con su pedido padre
    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    // Retorna el producto comprado en esta línea
    public Producto getProducto() {
        return producto;
    }

    // Asigna el producto de esta línea
    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    // Retorna la cantidad de unidades compradas
    public int getCantidad() {
        return cantidad;
    }

    // Asigna la cantidad con validación: no puede ser 0 ni negativa
    public void setCantidad(int cantidad) {
        if (cantidad <= 0) throw new ValidacionException("La cantidad debe ser mayor a 0."); // validación de negocio
        this.cantidad = cantidad;
    }

    // Retorna el precio unitario al momento de la compra
    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    // Asigna el precio unitario con validación: debe ser mayor a 0
    public void setPrecioUnitario(BigDecimal precioUnitario) {
        if (precioUnitario == null || precioUnitario.compareTo(BigDecimal.ZERO) <= 0) // compareTo es el correcto para BigDecimal (no <)
            throw new ValidacionException("El precio unitario debe ser mayor a 0.");
        this.precioUnitario = precioUnitario;
    }

    // Retorna si el ítem está activo
    public boolean isActivo() {
        return activo;
    }

    // Activa o desactiva (eliminación lógica) esta línea del pedido
    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    // Representación en texto para logs: muestra producto, cantidad, precio y subtotal calculado
    @Override
    public String toString() {
        return "Detallepedido{" + "idDetalle=" + idDetalle + ", productos=" + (producto != null ? producto.getNombreProducto() : "no cargado") + ", cantidad=" + cantidad + ", precioUnitario=" + precioUnitario + ", subtotal=" + getSubtotal() + '}';
    }

}
