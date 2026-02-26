
package logica;

import java.util.List;
import java.util.stream.Collectors;
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
@Table(name = "rol")
public class Rol {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private int idRol;

    @Column(name = "nombre_rol", nullable = false, length = 100)
    private String nombreRol;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "activo", nullable = false)
    private boolean activo;

    @OneToMany(mappedBy = "rol")
    private List<Rolpermiso> rolPermisos;

    @OneToMany(mappedBy = "rol")
    private List<Usuario> usuarios;

    public Rol() {
    }

    public Rol(int idRol, String nombreRol, String descripcion, boolean activo, List<Rolpermiso> rolPermisos, List<Usuario> usuarios) {
        this.idRol = idRol;
        this.nombreRol = nombreRol;
        this.descripcion = descripcion;
        this.activo = activo;
        this.rolPermisos = rolPermisos;
        this.usuarios = usuarios;
    }
    
    public List<Permiso> getListaPermisos() {
        if (rolPermisos == null) return null;
        return rolPermisos.stream().map(Rolpermiso::getPermiso)
                .filter(p -> p != null && p.isActivo()).collect(Collectors.toList());
    }
   
    public boolean tienePermiso(String nombrePermiso) {
        List<Permiso> permisos = getListaPermisos();
        if (permisos == null) return false;
        return permisos.stream().anyMatch(p -> p.getNombrePermiso().equalsIgnoreCase(nombrePermiso));
    }

    public boolean tieneRolPermisosCargados() { return rolPermisos != null; }
    public boolean tieneUsuariosCargados()    { return usuarios    != null; }

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


