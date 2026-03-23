
package enums;

/**
 * EstadoEntrega — Enum que define los estados posibles de un envío.
 * Se usa en la entidad Envio para representar el ciclo de vida de la entrega.
 * Flujo normal: PREPARANDO → EN_TRANSITO → ENTREGADO
 * Flujo excepcional: cualquier estado → DEVUELTO
 * Corresponde al campo estado_entrega en la tabla envio de MySQL.
 */
public enum EstadoEntrega {
    PREPARANDO,   // el paquete está siendo preparado en almacén
    EN_TRANSITO,  // el paquete fue entregado a la transportadora y está en camino
    ENTREGADO,    // el cliente recibió el paquete
    DEVUELTO      // el paquete fue devuelto (dirección incorrecta, rechazo, etc.)
}
