
package logica;

import enums.EstadoPedido;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 *
 * @author eduar
 */
@Entity
@Table(name = "pedido")
public class Pedido {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido")
    private int idPedido;

    @ManyToOne
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @Column(name = "fecha_pedido")
    private LocalDateTime fechaPedido;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoPedido estado;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "total", precision = 10, scale = 2)
    private BigDecimal total;

    @Column(name = "activo", nullable = false)
    private boolean activo;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL)
    private List<Detallepedido> detalles;

    @OneToOne(mappedBy = "pedido", cascade = CascadeType.ALL)
    private Pago pago;

    @OneToOne(mappedBy = "pedido", cascade = CascadeType.ALL)
    private Envio envio;

    public Pedido() {
    }

    public Pedido(int idPedido, Cliente cliente) {
        this.idPedido = idPedido; 
        this.cliente = cliente;
        this.estado = EstadoPedido.PENDIENTE; 
        this.total = BigDecimal.ZERO;
        this.fechaPedido = LocalDateTime.now(); 
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now(); 
        this.activo = true;
    }
    
    public BigDecimal calcularTotal() {
        if (detalles == null) return BigDecimal.ZERO;
        this.total = detalles.stream().filter(Detallepedido::isActivo)
                .map(Detallepedido::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        return this.total;
    }
    
    public void setEstado(EstadoPedido nuevoEstado) { this.estado = nuevoEstado; this.updatedAt = LocalDateTime.now(); }

    public boolean isPendiente() { return estado == EstadoPedido.PENDIENTE; }
    public boolean isPagado()    { return estado == EstadoPedido.PAGO; }
    public boolean isEnviado()   { return estado == EstadoPedido.ENVIADO; }
    public boolean isEntregado() { return estado == EstadoPedido.ENTREGADO; }
    public boolean isCancelado() { return estado == EstadoPedido.CANCELADO; }

    public boolean tieneDetallesCargados() { return detalles != null; }
    public boolean tienePagoCargado()      { return pago != null; }
    public boolean tieneEnvioCargado()     { return envio != null; }

    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public LocalDateTime getFechaPedido() {
        return fechaPedido;
    }

    public void setFechaPedido(LocalDateTime fechaPedido) {
        this.fechaPedido = fechaPedido;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public List<Detallepedido> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<Detallepedido> detalles) {
        this.detalles = detalles;
    }

    public Pago getPago() {
        return pago;
    }

    public void setPago(Pago pago) {
        this.pago = pago;
    }

    public Envio getEnvio() {
        return envio;
    }

    public void setEnvio(Envio envio) {
        this.envio = envio;
    }

    public EstadoPedido getEstado() {
        return estado;
    }  
    
    @Override public String toString() {
        return "Pedido{id=" + idPedido + ", estado=" + estado + ", total=" + total +
               ", cliente=" + (cliente != null ? cliente.getNombreCompleto() : "no cargado") +
               ", detalles=" + (detalles != null ? detalles.size() + " items" : "no cargados") +
               ", pago="     + (pago     != null ? pago.getEstadoPago()       : "sin pago") +
               ", envio="    + (envio    != null ? envio.getEstadoEntrega()   : "sin envio") + '}';
    }
}
