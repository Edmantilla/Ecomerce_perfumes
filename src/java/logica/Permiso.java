
package logica;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Permiso — Entidad JPA que mapea la tabla "permiso" de la base de datos.
 *
 * Representa una acción específica que un rol puede realizar en el sistema.
 * Ejemplos: VER_DASHBOARD, EDITAR_PRODUCTOS, GESTIONAR_ENVIOS, VER_USUARIOS.
 * Los permisos se agrupan por módulo para organizarlos en el panel admin.
 * Un permiso inactivo (activo=false) es ignorado por AuthHelper aunque esté asignado al rol.
 * Los permisos se asignan a los roles mediante la tabla de unión Rolpermiso.
 */
@Entity                    // esta clase es una entidad JPA (mapea una tabla)
@Table(name = "permiso")   // nombre de la tabla en MySQL
public class Permiso {
    
    @Id                                                    // clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY)    // AUTO_INCREMENT en MySQL
    @Column(name = "id_permiso")                           // columna id_permiso en la tabla
    private int idPermiso;

    @Column(name = "nombre_permiso", nullable = false, length = 100) // nombre del permiso, ej: "VER_DASHBOARD"
    // Este nombre es el que se verifica en AuthHelper.tienePermiso(request, "VER_DASHBOARD")
    private String nombrePermiso;

    @Column(name = "descripcion", length = 255)            // descripción legible para mostrar en el panel admin
    private String descripcion;

    @Column(name = "modulo", length = 100)                 // módulo al que pertenece (ej: "PRODUCTOS", "PEDIDOS")
    // Sirve para agrupar permisos visualmente en el panel de roles y permisos
    private String modulo;

    @Column(name = "activo", nullable = false)             // false = permiso deshabilitado (ignorado por AuthHelper)
    private boolean activo;

    public Permiso() {
    } // constructor vacío requerido por JPA

    // Constructor completo para crear un permiso con todos sus datos
    public Permiso(int idPermiso, String nombrePermiso, String descripcion, String modulo, boolean activo) {
        this.idPermiso = idPermiso;
        this.nombrePermiso = nombrePermiso;
        this.descripcion = descripcion;
        this.modulo = modulo;
        this.activo = activo;
    }
    
    // Retorna el ID del permiso (clave primaria)
    public int getIdPermiso() {
        return idPermiso;
    }

    // Asigna el ID (normalmente solo lo usa JPA internamente)
    public void setIdPermiso(int idPermiso) {
        this.idPermiso = idPermiso;
    }

    // Retorna el nombre del permiso (ej: "VER_DASHBOARD") — es el valor que compara AuthHelper
    public String getNombrePermiso() {
        return nombrePermiso;
    }

    // Asigna el nombre del permiso (sin validaciones adicionales)
    public void setNombrePermiso(String nombrePermiso) {
        this.nombrePermiso = nombrePermiso;
    }

    // Retorna la descripción legible del permiso
    public String getDescripcion() {
        return descripcion;
    }

    // Asigna la descripción del permiso
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    // Retorna el módulo al que pertenece el permiso
    public String getModulo() {
        return modulo;
    }

    // Asigna el módulo (ej: "PRODUCTOS", "PEDIDOS", "USUARIOS")
    public void setModulo(String modulo) {
        this.modulo = modulo;
    }

    // Retorna si el permiso está activo (true = se evalúa por AuthHelper)
    public boolean isActivo() {
        return activo;
    }

    // Activa o desactiva el permiso (SvPermisos con accion=togglePermiso)
    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    // Representación en texto para logs y depuración
    @Override
    public String toString() {
        return "Permiso{" + "idPermiso=" + idPermiso + ", nombrePermiso=" + nombrePermiso + ", descripcion=" + descripcion + ", modulo=" + modulo + ", activo=" + activo + '}';
    }

}
