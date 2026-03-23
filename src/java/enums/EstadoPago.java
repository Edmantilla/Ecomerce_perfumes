
package enums;

/**
 * EstadoPago — Enum que define los estados posibles de un pago.
 * Se usa en la entidad Pago para representar el ciclo de vida del pago.
 * Flujo normal: PENDIENTE → APROBADO
 * Flujo excepcional: PENDIENTE → RECHAZADO, o APROBADO → REEMBOLSADO
 * Corresponde al campo estado_pago en la tabla pago de MySQL.
 */
public enum EstadoPago {
    PENDIENTE,    // el pago aún no ha sido verificado ni procesado
    APROBADO,     // el pago fue verificado y aceptado
    RECHAZADO,    // el pago fue rechazado (fondos insuficientes, datos incorrectos, etc.)
    REEMBOLSADO   // el dinero fue devuelto al cliente (cancelación, devolución, etc.)
}
