
package logica;

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
 * Rolpermiso — Entidad JPA que mapea la tabla "rol_permiso" de la base de datos.
 *
 * Es la tabla de unión entre Rol y Permiso (relación N:M).
 * Cada fila representa la asignación de un permiso a un rol.
 * Ejemplo: el rol ADMIN tiene asignados los permisos VER_DASHBOARD, EDITAR_PRODUCTOS, etc.
 * Al asignar un permiso a un rol, todos los usuarios con ese rol ganan el permiso automáticamente.
 * Es gestionada desde el panel admin mediante SvPermisos con accion=asignar / accion=revocar.
 */
@Entity                       // esta clase es una entidad JPA (mapea una tabla)
@Table(name = "rol_permiso")  // tabla de unión entre rol y permiso en MySQL
public class Rolpermiso {
    
    @Id                                                    // clave primaria propia (más simple que clave compuesta)
    @GeneratedValue(strategy = GenerationType.IDENTITY)    // AUTO_INCREMENT en MySQL
    @Column(name = "id_rol_permiso")                       // columna id_rol_permiso en la tabla
    private int idRolPermiso;

    @ManyToOne                                             // relación N:1 — muchas asignaciones pueden ser del mismo rol
    @JoinColumn(name = "id_rol", nullable = false)         // FK al rol, no puede ser nula
    private Rol rol;

    @ManyToOne                                             // relación N:1 — muchas asignaciones pueden ser del mismo permiso
    @JoinColumn(name = "id_permiso", nullable = false)     // FK al permiso, no puede ser nula
    private Permiso permiso;

    @Column(name = "created_at")                           // fecha en que se asignó el permiso al rol
    private LocalDateTime createdAt;

    public Rolpermiso() {
    } // constructor vacío requerido por JPA
    
    // Constructor de negocio: crea la asignación rol-permiso con validación
    // Usado por SvPermisos al ejecutar accion=asignar
    // Lanza IllegalArgumentException si alguno de los dos es null (integridad de datos)
    public Rolpermiso(Rol rol, Permiso permiso){
        if (rol == null || permiso == null)                 // ambos son obligatorios
            throw new IllegalArgumentException("Rol y permiso son obligatorios.");
        this.rol = rol;
        this.permiso = permiso;
        this.createdAt = LocalDateTime.now();               // registrar cuándo se hizo la asignación
    }
    

    public int getIdRolPermiso() {
        return idRolPermiso;
    }

    public void setIdRolPermiso(int idRolPermiso) {
        this.idRolPermiso = idRolPermiso;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public Permiso getPermiso() {
        return permiso;
    }

    public void setPermiso(Permiso permiso) {
        this.permiso = permiso;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }


    
}
