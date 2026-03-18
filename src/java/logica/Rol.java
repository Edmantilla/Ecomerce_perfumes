
package logica;

import java.util.List;
import java.util.stream.Collectors;   // para filtrar la lista de permisos con Stream API
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * Rol — Entidad JPA que mapea la tabla "rol" de la base de datos.
 *
 * Representa el tipo de usuario en el sistema (ej: ADMIN, CLIENTE, SUPERVISOR).
 * Cada usuario tiene exactamente un rol asignado.
 * Un rol tiene una lista de permisos asignados a través de la tabla Rolpermiso.
 * Los roles inactivos (activo=false) no permiten login ni acceso al sistema.
 * Es gestionado desde el panel admin mediante SvPermisos.java.
 */
@Entity                 // esta clase es una entidad JPA (mapea una tabla)
@Table(name = "rol")    // nombre de la tabla en MySQL
public class Rol {
    
    @Id                                                    // clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY)    // AUTO_INCREMENT en MySQL
    @Column(name = "id_rol")                               // columna id_rol en la tabla
    private int idRol;

    @Column(name = "nombre_rol", nullable = false, length = 100) // nombre del rol, ej: "ADMIN", "CLIENTE"
    private String nombreRol;

    @Column(name = "descripcion", length = 255)            // descripción opcional del rol
    private String descripcion;

    @Column(name = "activo", nullable = false)             // false = rol deshabilitado
    private boolean activo;

    @OneToMany(mappedBy = "rol")                           // relación 1:N con la tabla de unión rolpermiso
    // A través de esta lista se accede a los permisos asignados al rol
    // Carga LAZY por defecto — solo se carga cuando se llama getListaPermisos()
    private List<Rolpermiso> rolPermisos;

    @OneToMany(mappedBy = "rol")                           // relación 1:N — un rol puede tener muchos usuarios
    // Usado por SvPermisos para verificar si el rol tiene usuarios activos antes de desactivarlo
    private List<Usuario> usuarios;

    public Rol() {
    } // constructor vacío requerido por JPA

    // Constructor completo para crear un rol con todos sus datos
    public Rol(int idRol, String nombreRol, String descripcion, boolean activo, List<Rolpermiso> rolPermisos, List<Usuario> usuarios) {
        this.idRol = idRol;
        this.nombreRol = nombreRol;
        this.descripcion = descripcion;
        this.activo = activo;
        this.rolPermisos = rolPermisos;
        this.usuarios = usuarios;
    }
    
    // Método de negocio: retorna la lista de permisos activos del rol
    // Filtra los Rolpermiso para quedarse solo con los Permiso que estén activos (activo=true)
    // Si la lista de rolPermisos no fue cargada desde BD (LAZY), retorna null
    public List<Permiso> getListaPermisos() {
        if (rolPermisos == null) return null; // si no se cargó desde BD, no hay permisos disponibles
        return rolPermisos.stream()
                .map(Rolpermiso::getPermiso)           // extraer el objeto Permiso de cada Rolpermiso
                .filter(p -> p != null && p.isActivo()) // ignorar permisos nulos o desactivados
                .collect(Collectors.toList());          // convertir el stream en una lista
    }
   
    // Método de negocio: verifica si este rol tiene un permiso específico (case-insensitive)
    // Es llamado por Usuario.tienePermiso() que es llamado por AuthHelper.tienePermiso()
    // Ejemplo: rol.tienePermiso("VER_DASHBOARD") devuelve true si el permiso está asignado y activo
    public boolean tienePermiso(String nombrePermiso) {
        List<Permiso> permisos = getListaPermisos();    // obtener permisos activos del rol
        if (permisos == null) return false;             // sin permisos cargados = sin acceso
        return permisos.stream().anyMatch(p -> p.getNombrePermiso().equalsIgnoreCase(nombrePermiso)); // comparar ignorando mayúsculas
    }

    // Verificadores de carga lazy: útiles para saber si las relaciones fueron cargadas antes de acceder
    public boolean tieneRolPermisosCargados() { return rolPermisos != null; } // true = la lista fue cargada desde BD
    public boolean tieneUsuariosCargados()    { return usuarios    != null; } // true = la lista fue cargada desde BD

    public int getIdRol() {
        return idRol;
    }

    public void setIdRol(int idRol) {
        this.idRol = idRol;
    }

    public String getNombreRol() {
        return nombreRol;
    }

    public void setNombreRol(String nombreRol) {
        this.nombreRol = nombreRol;
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

    public List<Rolpermiso> getRolPermisos() {
        return rolPermisos;
    }

    public void setRolPermisos(List<Rolpermiso> rolPermisos) {
        this.rolPermisos = rolPermisos;
    }

    public List<Usuario> getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(List<Usuario> usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    public String toString() {
        return "Rol{" + "nombreRol=" + nombreRol + ", activo=" + activo + '}';
    }
    
}


