
package logica;

import exceptions.ValidacionException;
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
@Table(name = "marca")
public class Marca {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_marca")
    private int idMarca;

    @Column(name = "nombre_marca", nullable = false, length = 100)
    private String nombreMarca;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "activo", nullable = false)
    private boolean activo;

    @OneToMany(mappedBy = "marca")
    private List<Producto> productos;

    public Marca() {
    }

    public Marca(int idMarca, String nombreMarca, String descripcion, boolean activo, List<Producto> productos) {
        this.idMarca = idMarca;
        this.nombreMarca = nombreMarca;
        this.descripcion = descripcion;
        this.activo = activo;
        this.productos = productos;
    }
    
    public int getIdMarca() {
        return idMarca;
    }

    public void setIdMarca(int idMarca) {
        this.idMarca = idMarca;
    }

    public String getNombreMarca() {
        return nombreMarca;
    }

    public void setNombreMarca(String nombreMarca) {
        if (nombreMarca == null || nombreMarca.isBlank())
            throw new ValidacionException("El nombre de la marca no puede estar vacío.");
        if (nombreMarca.length() > 100)
            throw new ValidacionException("El nombre de la marca no puede superar 100 caracteres.");
        this.nombreMarca = nombreMarca.trim();
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public List<Producto> getProductos() {
        return productos;
    }

    public void setProductos(List<Producto> productos) {
        this.productos = productos;
    }

    @Override
    public String toString() {
        return "Marca{" + "idMarca=" + idMarca + ", nombreMarca=" + nombreMarca + ", activo=" + activo + '}';
    }
    
}
