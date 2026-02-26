package logica;

import java.io.Serializable;
import java.util.Objects;

/**
 * Legacy class kept only for backward compatibility with old serialized data.
 * Rolpermiso now uses a simple generated primary key (idRolPermiso).
 */
@Deprecated
public class RolpermisoId implements Serializable {

    private int idRol;
    private int idPermiso;

    public RolpermisoId() {
    }

    public RolpermisoId(int idRol, int idPermiso) {
        this.idRol = idRol;
        this.idPermiso = idPermiso;
    }

    public int getIdRol() { return idRol; }
    public int getIdPermiso() { return idPermiso; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RolpermisoId)) return false;
        RolpermisoId that = (RolpermisoId) o;
        return idRol == that.idRol && idPermiso == that.idPermiso;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idRol, idPermiso);
    }
}
