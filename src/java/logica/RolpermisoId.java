package logica;

import java.io.Serializable; // requerido para clases que pueden ser serializadas
import java.util.Objects;    // para generar hashCode de forma segura

/**
 * RolpermisoId — Clase de clave compuesta legada (ya NO se usa activamente).
 *
 * @Deprecated: Esta clase fue diseñada originalmente para ser la clave primaria
 * compuesta de la tabla rol_permiso (combinación de id_rol + id_permiso).
 *
 * La clase Rolpermiso fue refactorizada para usar un ID simple generado (AUTO_INCREMENT),
 * por lo que esta clase solo se mantiene por compatibilidad con datos serializados anteriores.
 * NO debe usarse en código nuevo.
 */
@Deprecated // marca que esta clase no debe usarse en código nuevo
public class RolpermisoId implements Serializable {

    private int idRol;     // ID del rol (parte de la clave compuesta original)
    private int idPermiso; // ID del permiso (parte de la clave compuesta original)

    public RolpermisoId() {
    } // constructor vacío requerido para serializar/deserializar

    // Constructor con los dos IDs que formaban la clave compuesta
    public RolpermisoId(int idRol, int idPermiso) {
        this.idRol = idRol;
        this.idPermiso = idPermiso;
    }

    public int getIdRol() { return idRol; }         // retorna el ID del rol
    public int getIdPermiso() { return idPermiso; } // retorna el ID del permiso

    // equals() es obligatorio en una clave compuesta: JPA lo usa para comparar identidades
    // Dos RolpermisoId son iguales si tienen el mismo idRol E idPermiso
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;                          // misma referencia en memoria
        if (!(o instanceof RolpermisoId)) return false;      // objeto de tipo diferente
        RolpermisoId that = (RolpermisoId) o;
        return idRol == that.idRol && idPermiso == that.idPermiso; // comparar ambos campos
    }

    // hashCode() debe ser consistente con equals(): objetos iguales deben tener el mismo hash
    // Objects.hash() combina los dos campos para generar un hash único
    @Override
    public int hashCode() {
        return Objects.hash(idRol, idPermiso);
    }
}
