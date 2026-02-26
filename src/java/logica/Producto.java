
package logica;

import exceptions.StockInsuficienteException;
import exceptions.ValidacionException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 *
 * @author eduar
 */
@Entity
@Table(name = "producto")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private int idProducto;

    @ManyToOne
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;

    @ManyToOne
    @JoinColumn(name = "id_marca", nullable = false)
    private Marca marca;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "stock", nullable = false)
    private int stock;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "nombre_producto", nullable = false, length = 200)
    private String nombreProducto;

    @Column(name = "precio", nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(name = "activo", nullable = false)
    private boolean activo;

    @Column(name = "imagen_url", length = 500)
    private String imagenUrl;

    public Producto() {
    }

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
   
    
    //Metodos del negocio
    
    public boolean isDisponible() { return activo && stock > 0; }

    public void reducirStock(int cantidad) {
        if (cantidad <= 0)
            throw new ValidacionException("La cantidad debe ser mayor a 0.");
        if (this.stock < cantidad)
            throw new StockInsuficienteException(nombreProducto, this.stock, cantidad);
        this.stock -= cantidad;
        this.updatedAt = LocalDateTime.now();
    }

    public void aumentarStock(int cantidad) {
        if (cantidad <= 0)
            throw new ValidacionException("La cantidad debe ser mayor a 0.");
        this.stock += cantidad;
        this.updatedAt = LocalDateTime.now();
    }

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

    @Override
    public String toString() {
        return "Producto{" + "idProducto=" + idProducto + ", categoria=" + 
                (categoria != null ? categoria.getNombreCategoria(): "no cargada") + ", marca=" + (marca != null ? marca.getNombreMarca(): "no cargada") 
                + ", stock=" + stock + ", nombreProducto=" + nombreProducto + ", precio=" + precio + ", activo=" + activo + '}';
    } 
    
}

