
package enums;

/**
 * EstadoPedido — Enum que define los estados posibles de un pedido.
 * Se usa en la entidad Pedido para representar el ciclo de vida de una compra.
 * Flujo normal: PENDIENTE → PROCESANDO → PAGO → ENVIADO → ENTREGADO
 * Flujo excepcional: cualquier estado → CANCELADO
 * Corresponde al campo estado en la tabla pedido de MySQL.
 */
public enum EstadoPedido {
    PENDIENTE,   // pedido recién creado, aún no procesado
    PROCESANDO,  // el administrador está preparando el pedido
    PAGO,        // se registró un pago para este pedido
    ENVIADO,     // el pedido fue despachado al cliente
    ENTREGADO,   // el cliente confirmó la recepción del pedido
    CANCELADO    // el pedido fue cancelado (por el cliente o el admin)
}

