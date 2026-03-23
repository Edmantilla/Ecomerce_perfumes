
package logica;

import exceptions.StockInsuficienteException; // excepción cuando no hay stock suficiente
import exceptions.ValidacionException;        // excepción para datos inválidos
import java.math.BigDecimal;                  // tipo preciso para precios monetarios
import java.time.LocalDateTime;               // manejo de fechas con hora
import javax.persistence.Column;              // mapeo de columnas de la tabla
import javax.persistence.Entity;              // marca esta clase como entidad JPA
import javax.persistence.GeneratedValue;      // generación automática del ID
import javax.persistence.GenerationType;      // estrategia IDENTITY = autoincrement
import javax.persistence.Id;                  // clave primaria
import javax.persistence.JoinColumn;          // columna FK en esta tabla
import javax.persistence.ManyToOne;           // relación muchos-a-uno
import javax.persistence.Table;               // nombre de la tabla en MySQL

/**
 * Producto — Entidad JPA que representa un perfume en el catálogo de la tienda.
 * Mapea la tabla 'producto' de MySQL.
 * Contiene nombre, descripción, precio actual, stock, imagen y estado.
 * Se relaciona con Categoria y Marca (muchos productos pertenecen a una categoría/marca).
 * El campo 'precio' es el precio ACTUAL del catálogo (puede cambiar con el tiempo).
 * El precio al momento de la venta se guarda en Detallepedido.precioUnitario (snapshot).
 */
@Entity                            // esta clase es una entidad JPA
@Table(name = "producto")          // tabla MySQL: 'producto'
public class Producto {

    @Id                                                        // clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY)         // autoincrement en MySQL
    @Column(name = "id_producto")                               // columna: id_producto
    private int idProducto;                                     // ID único del producto

    // Relación N:1 con Categoria (muchos productos pertenecen a una categoría)
    @ManyToOne                                                  // muchos productos → una categoría
    @JoinColumn(name = "id_categoria", nullable = false)        // FK: id_categoria, obligatorio
    private Categoria categoria;                                // categoría del perfume (Eau de Parfum, etc.)

    // Relación N:1 con Marca (muchos productos pertenecen a una marca)
    @ManyToOne                                                  // muchos productos → una marca
    @JoinColumn(name = "id_marca", nullable = false)            // FK: id_marca, obligatorio
    private Marca marca;                                        // marca del perfume (Chanel, Dior, etc.)

    @Column(name = "descripcion", length = 255)                 // columna: descripcion, máx 255 chars
    private String descripcion;                                 // descripción corta del perfume

    @Column(name = "stock", nullable = false)                   // columna: stock, obligatorio
    private int stock;                                          // unidades disponibles en inventario

    @Column(name = "created_at")                                // columna: created_at
    private LocalDateTime createdAt;                            // fecha de creación del producto

    @Column(name = "updated_at")                                // columna: updated_at
    private LocalDateTime updatedAt;                            // última actualización

    @Column(name = "nombre_producto", nullable = false, length = 200) // obligatorio, máx 200 chars
    private String nombreProducto;                              // nombre del perfume (ej: "Sauvage EDP")

    @Column(name = "precio", nullable = false, precision = 10, scale = 2) // DECIMAL(10,2) obligatorio
    private BigDecimal precio;                                  // precio actual del catálogo en COP

    @Column(name = "activo", nullable = false)                  // columna: activo
    private boolean activo;                                     // true=visible en tienda, false=oculto

    @Column(name = "imagen_url", length = 500)                  // columna: imagen_url, máx 500 chars
    private String imagenUrl;                                   // URL de la imagen del producto

    // Constructor vacío requerido por JPA
    public Producto() {
    }

    // Constructor completo con todos los campos (excepto imagenUrl)
    public Producto(int idProducto, Categoria categoria, Marca marca, String descripcion, int stock, LocalDateTime createdAt, LocalDateTime updatedAt, String nombreProducto, BigDecimal precio, boolean activo) {
        this.idProducto = idProducto;
        this.categoria = categoria;
        this.marca = marca;
        this.descripcion = descripcion;
        this.stock = stock;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.nombreProducto = nombreProducto;
        this.precio = precio;
        this.activo = activo;
    }
   
    
    // ========== Métodos de negocio ==========
    
    // Verifica si el producto está disponible para la venta (activo Y con stock > 0)
    public boolean isDisponible() { return activo && stock > 0; }

    // Reduce el stock del producto tras una compra
    // Lanza StockInsuficienteException si no hay suficientes unidades
    // Lanza ValidacionException si la cantidad es <= 0
    public void reducirStock(int cantidad) {
        if (cantidad <= 0)
            throw new ValidacionException("La cantidad debe ser mayor a 0.");
        if (this.stock < cantidad)
            throw new StockInsuficienteException(nombreProducto, this.stock, cantidad);
        this.stock -= cantidad;              // resta las unidades vendidas
        this.updatedAt = LocalDateTime.now(); // actualiza timestamp
    }

    // Aumenta el stock del producto (reposición de inventario)
    // Lanza ValidacionException si la cantidad es <= 0
    public void aumentarStock(int cantidad) {
        if (cantidad <= 0)
            throw new ValidacionException("La cantidad debe ser mayor a 0.");
        this.stock += cantidad;              // suma las unidades
        this.updatedAt = LocalDateTime.now(); // actualiza timestamp
    }

    // Verifican si las relaciones lazy ya fueron cargadas por JPA
    public boolean tieneCategoriaCargada() { return categoria != null; }
    public boolean tieneMarcaCargada()     { return marca     != null; }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public Marca getMarca() {
        return marca;
    }

    public void setMarca(Marca marca) {
        this.marca = marca;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getStock() {
        return stock;
    }

    // Setter con validación: el stock no puede ser negativo
    public void setStock(int stock) {
        if (stock < 0)
            throw new ValidacionException("El stock no puede ser negativo.");
        this.stock = stock;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    // Setter con validación: nombre obligatorio, máx 200 caracteres, se recorta
    public void setNombreProducto(String nombreProducto) {
        if (nombreProducto == null || nombreProducto.isBlank())
            throw new ValidacionException("El nombre del producto no puede estar vacío.");
        if (nombreProducto.length() > 200)
            throw new ValidacionException("El nombre del producto no puede superar 200 caracteres.");
        this.nombreProducto = nombreProducto.trim();
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    // Setter con validación: el precio debe ser mayor a 0
    public void setPrecio(BigDecimal precio) {
        if (precio == null || precio.compareTo(BigDecimal.ZERO) <= 0)
            throw new ValidacionException("El precio debe ser mayor a 0.");
        this.precio = precio;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    // Representación en texto del producto (para logs y debugging)
    @Override
    public String toString() {
        return "Producto{" + "idProducto=" + idProducto + ", categoria=" + 
                (categoria != null ? categoria.getNombreCategoria(): "no cargada") + ", marca=" + (marca != null ? marca.getNombreMarca(): "no cargada") 
                + ", stock=" + stock + ", nombreProducto=" + nombreProducto + ", precio=" + precio + ", activo=" + activo + '}';
    } 
    
}

