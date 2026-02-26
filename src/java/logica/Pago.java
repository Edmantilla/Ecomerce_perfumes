
package logica;

import enums.EstadoPago;
import enums.EstadoPedido;
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
 *
 * @author eduar
 */
@Entity
@Table(name = "pago")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago")
    private int idPago;

    @OneToOne
    @JoinColumn(name = "id_pedido", nullable = false)
    private Pedido pedido;

    @Column(name = "fecha_pago")
    private LocalDateTime fechaPago;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_pago", nullable = false, length = 20)
    private EstadoPago estadoPago;

    @Column(name = "metodo_pago", length = 50)
    private String metodoPago;

    @Column(name = "monto_pagado", precision = 10, scale = 2)
    private BigDecimal montoPagado;

    @Column(name = "referencia_transaccion", length = 100)
    private String referenciaTransaccion;

    @Column(name = "activo", nullable = false)
    private boolean activo;

    public Pago() {
    }

    public void aprobar()    { this.estadoPago = EstadoPago.APROBADO;    if (pedido != null) pedido.setEstado(EstadoPedido.PAGO); }
    public void rechazar()   { this.estadoPago = EstadoPago.RECHAZADO; }
    public void reembolsar() { this.estadoPago = EstadoPago.REEMBOLSADO; }
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

