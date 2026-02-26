
package logica;

import exceptions.ValidacionException;
import java.io.Serializable;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 *
 * @author eduar
 */
@Entity
@Table(name = "categoria")
public class Categoria implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria")
    private int idCategoria;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "nombre_categoria", nullable = false, length = 100)
    private String nombreCategoria;

    @Column(name = "activo", nullable = false)
    private boolean activo;

    @OneToMany(mappedBy = "categoria")
    private List<Producto> productos;

    public Categoria() {
    }

    public Categoria(int idCategoria, String descripcion, String nombreCategoria, boolean activo, List<Producto> productos) {
        this.idCategoria = idCategoria;
        this.descripcion = descripcion;
        this.nombreCategoria = nombreCategoria;
        this.activo = activo;
        this.productos = productos;
    }
    

    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getNombreCategoria() {
        return nombreCategoria;
    }

    public void setNombreCategoria(String nombreCategoria) {
        
        if(nombreCategoria == null || nombreCategoria.isBlank())
            throw new ValidacionException("El nombre de la categoria no puede estar vacio");
        if(nombreCategoria.length()> 100)
            throw new ValidacionException("El nombre de la categoria no puede superar los 100 caracteres.");
        this.nombreCategoria = nombreCategoria.trim();
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public List getProductos() {
        return productos;
    }

    public void setProductos(List productos) {
        this.productos = productos;
    }

    @Override
    public String toString() {
        return "Categoria{" + "idCategoria=" + idCategoria + ", descripcion=" + descripcion + ", nombreCategoria=" + nombreCategoria + ", activo=" + activo + 
                ", productos=" + (productos != null ? productos.size()+ " cargados":" No cargados") + '}';
      
    }

}
