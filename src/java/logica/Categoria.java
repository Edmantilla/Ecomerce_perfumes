
package logica;

import exceptions.ValidacionException; // excepción personalizada para errores de reglas de negocio
import java.io.Serializable;           // permite serializar el objeto (requerido por JPA en algunos contextos)
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * Categoria — Entidad JPA que mapea la tabla "categoria" de la base de datos.
 *
 * Representa una clasificación de productos (ej: Mujer, Hombre, Unisex).
 * Una categoría puede tener muchos productos asociados (relación 1:N).
 * Las categorías inactivas (activo=false) no aparecen en el catálogo público.
 */
@Entity                         // le dice a JPA que esta clase es una tabla de la BD
@Table(name = "categoria")      // nombre exacto de la tabla en MySQL
public class Categoria implements Serializable {

    @Id                                                    // esta es la clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY)    // el valor lo genera MySQL con AUTO_INCREMENT
    @Column(name = "id_categoria")                         // mapea al campo id_categoria de la tabla
    private int idCategoria;

    @Column(name = "descripcion", length = 255)            // campo opcional, máximo 255 caracteres en BD
    private String descripcion;

    @Column(name = "nombre_categoria", nullable = false, length = 100) // nombre obligatorio, máximo 100 chars
    private String nombreCategoria;

    @Column(name = "activo", nullable = false)             // true = visible en tienda; false = oculta
    private boolean activo;

    @OneToMany(mappedBy = "categoria")                     // relación 1:N: una categoría tiene muchos productos
    // mappedBy="categoria" indica que la FK (id_categoria) está del lado de Producto, no aquí
    // esta lista se carga de forma LAZY por defecto (solo cuando se accede a ella)
    private List<Producto> productos;

    public Categoria() {
    } // constructor vacío requerido obligatoriamente por JPA para poder crear instancias

    // Constructor completo para crear una categoría con todos sus datos de una vez
    public Categoria(int idCategoria, String descripcion, String nombreCategoria, boolean activo, List<Producto> productos) {
        this.idCategoria = idCategoria;
        this.descripcion = descripcion;
        this.nombreCategoria = nombreCategoria;
        this.activo = activo;
        this.productos = productos;
    }
    

    // Retorna el ID de la categoría (clave primaria)
    public int getIdCategoria() {
        return idCategoria;
    }

    // Asigna el ID (normalmente solo lo usa JPA internamente)
    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    // Retorna la descripción opcional de la categoría
    public String getDescripcion() {
        return descripcion;
    }

    // Asigna la descripción sin validaciones (puede ser null)
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    // Retorna el nombre visible de la categoría
    public String getNombreCategoria() {
        return nombreCategoria;
    }

    // Asigna el nombre con validaciones de negocio:
    public void setNombreCategoria(String nombreCategoria) {
        
        if(nombreCategoria == null || nombreCategoria.isBlank())         // no puede ser vacío ni solo espacios
            throw new ValidacionException("El nombre de la categoria no puede estar vacio");
        if(nombreCategoria.length()> 100)                                // no puede superar el límite de la BD
            throw new ValidacionException("El nombre de la categoria no puede superar los 100 caracteres.");
        this.nombreCategoria = nombreCategoria.trim();                   // elimina espacios al inicio y final
    }

    // Retorna si la categoría está activa (true) o inactiva (false)
    public boolean isActivo() {
        return activo;
    }

    // Activa o desactiva la categoría (usado por SvCategorias con accion=desactivar)
    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    // Retorna la lista de productos de esta categoría (puede estar sin cargar si no se hizo JOIN)
    public List getProductos() {
        return productos;
    }

    // Asigna la lista de productos (usado internamente por JPA)
    public void setProductos(List productos) {
        this.productos = productos;
    }

    // Representación en texto para logs y depuración
    // Si la lista de productos no fue cargada desde BD, muestra "No cargados" en vez de dar NullPointerException
    @Override
    public String toString() {
        return "Categoria{" + "idCategoria=" + idCategoria + ", descripcion=" + descripcion + ", nombreCategoria=" + nombreCategoria + ", activo=" + activo + 
                ", productos=" + (productos != null ? productos.size()+ " cargados":" No cargados") + '}';
      
    }

}
