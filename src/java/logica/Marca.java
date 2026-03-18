
package logica;

import exceptions.ValidacionException; // excepción personalizada para errores de reglas de negocio
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * Marca — Entidad JPA que mapea la tabla "marca" de la base de datos.
 *
 * Representa un fabricante/diseñador de perfumes (ej: Chanel, Dior, Xerjoff).
 * Cada marca tiene su propia página JSP generada automáticamente al crearla (paginaUrl).
 * Se separan por género (HOMBRE/MUJER) para organizar el menú de navegación.
 * Las marcas inactivas no aparecen en el navbar ni en el catálogo.
 * Su JSP muestra los productos filtrados por nombre de marca desde SvProductos.
 */
@Entity                 // esta clase es una entidad JPA (mapea una tabla)
@Table(name = "marca")  // nombre de la tabla en MySQL
public class Marca {
    
    @Id                                                    // clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY)    // AUTO_INCREMENT en MySQL
    @Column(name = "id_marca")                             // columna id_marca en la tabla
    private int idMarca;

    @Column(name = "nombre_marca", nullable = false, length = 100) // nombre visible, obligatorio, máx 100 chars
    private String nombreMarca;

    @Column(name = "descripcion", length = 255)            // descripción opcional de la marca
    private String descripcion;

    @Column(name = "genero", nullable = false, length = 6) // "HOMBRE" o "MUJER" — clasifica en el navbar
    private String genero;

    @Column(name = "pagina_url", length = 150)             // ruta relativa del JSP de esta marca (ej: "Chanel.jsp")
    // Este JSP es generado automáticamente por SvMarcas al crear la marca
    private String paginaUrl;

    @Column(name = "activo", nullable = false)             // false = marca oculta en tienda y navbar
    private boolean activo;

    @OneToMany(mappedBy = "marca")                         // relación 1:N — una marca tiene muchos productos
    // mappedBy="marca" indica que la FK (id_marca) está en la tabla producto
    // carga LAZY por defecto — no se carga hasta que se acceda a la lista
    private List<Producto> productos;

    public Marca() {
    } // constructor vacío requerido por JPA

    // Constructor básico (sin género ni paginaUrl, para compatibilidad con código anterior)
    public Marca(int idMarca, String nombreMarca, String descripcion, boolean activo, List<Producto> productos) {
        this.idMarca = idMarca;
        this.nombreMarca = nombreMarca;
        this.descripcion = descripcion;
        this.activo = activo;
        this.productos = productos;
    }
    
    // Retorna el ID de la marca (clave primaria)
    public int getIdMarca() {
        return idMarca;
    }

    // Asigna el ID (normalmente solo lo usa JPA internamente)
    public void setIdMarca(int idMarca) {
        this.idMarca = idMarca;
    }

    // Retorna el nombre visible de la marca
    public String getNombreMarca() {
        return nombreMarca;
    }

    // Asigna el nombre con validaciones de negocio
    public void setNombreMarca(String nombreMarca) {
        if (nombreMarca == null || nombreMarca.isBlank())          // no puede ser vacío
            throw new ValidacionException("El nombre de la marca no puede estar vacío.");
        if (nombreMarca.length() > 100)                            // no puede superar el límite de la BD
            throw new ValidacionException("El nombre de la marca no puede superar 100 caracteres.");
        this.nombreMarca = nombreMarca.trim();                     // elimina espacios al inicio y final
    }

    // Retorna la descripción opcional de la marca
    public String getDescripcion() {
        return descripcion;
    }

    // Asigna la descripción sin validaciones (puede ser null o vacía)
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    // Retorna el género de la marca; si no está definido, retorna "HOMBRE" como valor por defecto
    public String getGenero() {
        return genero != null ? genero : "HOMBRE"; // evita retornar null al frontend
    }

    // Asigna el género: solo acepta "MUJER" (case-insensitive); cualquier otro valor se convierte a "HOMBRE"
    // Esto garantiza que el campo siempre tenga uno de los dos valores válidos
    public void setGenero(String genero) {
        this.genero = (genero != null && genero.equalsIgnoreCase("MUJER")) ? "MUJER" : "HOMBRE";
    }

    // Retorna la ruta relativa del JSP de esta marca (ej: "Chanel.jsp", "paco_rabanne.jsp")
    public String getPaginaUrl() {
        return paginaUrl;
    }

    // Asigna la ruta del JSP (se guarda al crear la marca en SvMarcas)
    public void setPaginaUrl(String paginaUrl) {
        this.paginaUrl = paginaUrl;
    }

    // Retorna si la marca está activa (visible en tienda y navbar)
    public boolean isActivo() {
        return activo;
    }

    // Activa o desactiva la marca (SvMarcas verifica que no tenga productos activos antes de desactivar)
    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    // Retorna la lista de productos de esta marca (puede estar sin cargar si no se hizo JOIN)
    public List<Producto> getProductos() {
        return productos;
    }

    // Asigna la lista de productos (usado internamente por JPA)
    public void setProductos(List<Producto> productos) {
        this.productos = productos;
    }

    // Representación en texto para logs y depuración
    @Override
    public String toString() {
        return "Marca{" + "idMarca=" + idMarca + ", nombreMarca=" + nombreMarca + ", activo=" + activo + '}';
    }
    
}
