
package enums;

/**
 * TipoTelefono — Enum que define los tipos de teléfono que puede registrar un cliente.
 * Se usa en la entidad Telefonocliente para clasificar los números de contacto.
 * Corresponde al campo tipo_telefono en la tabla telefono_cliente de MySQL.
 */
public enum TipoTelefono {
    CELULAR,  // teléfono móvil / celular
    FIJO,     // teléfono fijo / residencial
    TRABAJO   // teléfono laboral / de oficina
}
