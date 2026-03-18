
package logica;

import enums.EstadoPago;   // enum con estados del pago: PENDIENTE, APROBADO, RECHAZADO, REEMBOLSADO
import enums.EstadoPedido; // para sincronizar el estado del pedido al aprobar el pago
import java.math.BigDecimal;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * Pago — Entidad JPA que mapea la tabla "pago" de la base de datos.
 *
 * Representa el pago asociado a un pedido (relación 1:1 con Pedido).
 * Registra el método de pago, el monto pagado, la referencia de la transacción
 * y el estado del pago (PENDIENTE, APROBADO, RECHAZADO, REEMBOLSADO).
 * Al aprobar el pago, el estado del pedido cambia automáticamente a PAGO.
 * Actualmente la integración con pasarelas de pago externas no está implementada.
 */
@Entity                 // esta clase es una entidad JPA (mapea una tabla)
@Table(name = "pago")   // nombre de la tabla en MySQL
public class Pago {

    @Id                                                    // clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY)    // AUTO_INCREMENT en MySQL
    @Column(name = "id_pago")                              // columna id_pago en la tabla
    private int idPago;

    @OneToOne                                              // relación 1:1 — un pago pertenece a un solo pedido
    @JoinColumn(name = "id_pedido", nullable = false)      // FK al pedido, no puede ser nula
    private Pedido pedido;

    @Column(name = "fecha_pago")                           // fecha y hora en que se realizó el pago
    private LocalDateTime fechaPago;

    @Enumerated(EnumType.STRING)                           // guarda el nombre del enum como texto en BD
    @Column(name = "estado_pago", nullable = false, length = 20) // ej: "PENDIENTE", "APROBADO", "RECHAZADO"
    private EstadoPago estadoPago;

    @Column(name = "metodo_pago", length = 50)             // ej: "TARJETA", "TRANSFERENCIA", "EFECTIVO"
    private String metodoPago;

    @Column(name = "monto_pagado", precision = 10, scale = 2) // monto real pagado (puede diferir del total si hubo descuento)
    private BigDecimal montoPagado;

    @Column(name = "referencia_transaccion", length = 100) // código de transacción de la pasarela de pago
    private String referenciaTransaccion;

    @Column(name = "activo", nullable = false)             // false = pago anulado o eliminado lógicamente
    private boolean activo;

    public Pago() {
    } // constructor vacío requerido por JPA

    // Método de negocio: aprueba el pago y sincroniza el estado del pedido a PAGO
    // Es el único método que cambia el estado del pedido cuando el pago se confirma
    public void aprobar()    { this.estadoPago = EstadoPago.APROBADO; if (pedido != null) pedido.setEstado(EstadoPedido.PAGO); }

    // Método de negocio: marca el pago como rechazado (ej: tarjeta sin fondos)
    public void rechazar()   { this.estadoPago = EstadoPago.RECHAZADO; }

    // Método de negocio: marca el pago como reembolsado al cliente
    public void reembolsar() { this.estadoPago = EstadoPago.REEMBOLSADO; }

    // Verifica rápidamente si el pago ya fue aprobado (usado en validaciones)
    public boolean isAprobado() { return estadoPago == EstadoPago.APROBADO; }
    
    public Pago(int idPago, Pedido pedido, LocalDateTime fechaPago, EstadoPago estadoPago, String metodoPago, BigDecimal montoPagado, String referenciaTransaccion, boolean activo) {
        this.idPago = idPago;
        this.pedido = pedido;
        this.fechaPago = fechaPago;
        this.estadoPago = estadoPago;
        this.metodoPago = metodoPago;
        this.montoPagado = montoPagado;
        this.referenciaTransaccion = referenciaTransaccion;
        this.activo = activo;
    }

    public int getIdPago() {
        return idPago;
    }

    public void setIdPago(int idPago) {
        this.idPago = idPago;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public LocalDateTime getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDateTime fechaPago) {
        this.fechaPago = fechaPago;
    }

    public EstadoPago getEstadoPago() {
        return estadoPago;
    }

    public void setEstadoPago(EstadoPago estadoPago) {
        this.estadoPago = estadoPago;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public BigDecimal getMontoPagado() {
        return montoPagado;
    }

    public void setMontoPagado(BigDecimal montoPagado) {
        this.montoPagado = montoPagado;
    }

    public String getReferenciaTransaccion() {
        return referenciaTransaccion;
    }

    public void setReferenciaTransaccion(String referenciaTransaccion) {
        this.referenciaTransaccion = referenciaTransaccion;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return "Pago{" + "idPago=" + idPago + ", pedido=" + (pedido != null ? pedido.getIdPedido() : "no cargado") + ", estadoPago=" + estadoPago + ", metodoPago=" + metodoPago + ", montoPagado=" + montoPagado + '}';
    }
}

