
package logica;

import enums.EstadoPedido;                   // enum con los estados del pedido
import java.math.BigDecimal;                 // tipo preciso para montos monetarios
import java.time.LocalDateTime;              // manejo de fechas con hora
import java.util.List;                       // lista de detalles del pedido
import javax.persistence.CascadeType;        // propagación de operaciones en cascada
import javax.persistence.Column;             // mapeo de columnas de la tabla
import javax.persistence.Entity;             // marca esta clase como entidad JPA
import javax.persistence.EnumType;           // tipo de mapeo del enum (STRING = texto)
import javax.persistence.Enumerated;         // mapea un campo Java enum a columna SQL
import javax.persistence.GeneratedValue;     // generación automática del ID
import javax.persistence.GenerationType;     // estrategia IDENTITY = autoincrement
import javax.persistence.Id;                 // clave primaria
import javax.persistence.JoinColumn;         // columna FK en esta tabla
import javax.persistence.ManyToOne;          // relación muchos-a-uno
import javax.persistence.OneToMany;          // relación uno-a-muchos
import javax.persistence.OneToOne;           // relación uno-a-uno
import javax.persistence.Table;              // nombre de la tabla en MySQL

/**
 * Pedido — Entidad JPA que representa una compra realizada por un cliente.
 * Mapea la tabla 'pedido' de MySQL.
 * Contiene el estado del pedido, total, fecha y relaciones con:
 *   - Cliente (quién hizo la compra)
 *   - Detallepedido (productos comprados)
 *   - Pago (información del pago)
 *   - Envio (información del envío)
 * Ciclo de vida: PENDIENTE → PROCESANDO → PAGO → ENVIADO → ENTREGADO (o CANCELADO)
 */
@Entity                          // esta clase es una entidad JPA
@Table(name = "pedido")          // tabla MySQL: 'pedido'
public class Pedido {
    
    @Id                                                       // clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY)        // autoincrement en MySQL
    @Column(name = "id_pedido")                                // columna: id_pedido
    private int idPedido;                                      // ID único del pedido

    // Relación N:1 con Cliente (muchos pedidos pertenecen a un cliente)
    @ManyToOne                                                 // muchos pedidos → un cliente
    @JoinColumn(name = "id_cliente", nullable = false)         // FK: id_cliente en tabla pedido
    private Cliente cliente;                                   // cliente que realizó la compra

    @Column(name = "fecha_pedido")                             // columna: fecha_pedido
    private LocalDateTime fechaPedido;                         // cuándo se hizo el pedido

    @Enumerated(EnumType.STRING)                               // almacena el enum como texto ("PENDIENTE", "ENVIADO", etc.)
    @Column(name = "estado", nullable = false, length = 20)    // columna: estado, obligatorio
    private EstadoPedido estado;                               // estado actual del pedido

    @Column(name = "created_at")                               // columna: created_at
    private LocalDateTime createdAt;                           // fecha de creación

    @Column(name = "updated_at")                               // columna: updated_at
    private LocalDateTime updatedAt;                           // última actualización

    @Column(name = "total", precision = 10, scale = 2)         // DECIMAL(10,2) en MySQL
    private BigDecimal total;                                  // monto total del pedido en COP

    @Column(name = "activo", nullable = false)                 // columna: activo
    private boolean activo;                                    // true=activo, false=eliminado lógicamente

    // Relación 1:N con Detallepedido (un pedido tiene múltiples productos)
    // CascadeType.ALL: al guardar el pedido, también se guardan sus detalles
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL)
    private List<Detallepedido> detalles;                      // lista de productos comprados

    // Relación 1:1 con Pago (un pedido tiene un solo pago)
    @OneToOne(mappedBy = "pedido", cascade = CascadeType.ALL)
    private Pago pago;                                        // pago asociado al pedido

    // Relación 1:1 con Envio (un pedido tiene un solo envío)
    @OneToOne(mappedBy = "pedido", cascade = CascadeType.ALL)
    private Envio envio;                                      // envío asociado al pedido

    // Constructor vacío requerido por JPA
    public Pedido() {
    }

    // Constructor de negocio: crea un pedido nuevo con estado PENDIENTE
    // Se usa en SvCompra al confirmar el carrito de compras
    public Pedido(int idPedido, Cliente cliente) {
        this.idPedido = idPedido; 
        this.cliente = cliente;                    // cliente que compra
        this.estado = EstadoPedido.PENDIENTE;      // estado inicial siempre PENDIENTE
        this.total = BigDecimal.ZERO;              // total se calcula después con los detalles
        this.fechaPedido = LocalDateTime.now();    // fecha actual como fecha del pedido
        this.createdAt = LocalDateTime.now();      // timestamp de creación
        this.updatedAt = LocalDateTime.now();      // timestamp de actualización
        this.activo = true;                        // activo por defecto
    }
    
    // Calcula el total del pedido sumando los subtotales de todos los detalles activos
    // Cada detalle tiene: cantidad * precio_unitario = subtotal
    // Solo suma detalles con activo=true (ignora eliminados lógicamente)
    public BigDecimal calcularTotal() {
        if (detalles == null) return BigDecimal.ZERO;
        this.total = detalles.stream().filter(Detallepedido::isActivo) // solo detalles activos
                .map(Detallepedido::getSubtotal)                      // obtener subtotal de cada uno
                .reduce(BigDecimal.ZERO, BigDecimal::add);            // sumar todos
        return this.total;
    }
    
    // Setter de estado: actualiza el estado y la fecha de modificación automáticamente
    public void setEstado(EstadoPedido nuevoEstado) { this.estado = nuevoEstado; this.updatedAt = LocalDateTime.now(); }

    // Métodos de conveniencia para verificar el estado actual del pedido
    public boolean isPendiente() { return estado == EstadoPedido.PENDIENTE; }
    public boolean isPagado()    { return estado == EstadoPedido.PAGO; }
    public boolean isEnviado()   { return estado == EstadoPedido.ENVIADO; }
    public boolean isEntregado() { return estado == EstadoPedido.ENTREGADO; }
    public boolean isCancelado() { return estado == EstadoPedido.CANCELADO; }

    // Verifican si las relaciones lazy ya fueron cargadas por JPA
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
    
    // Representación en texto del pedido (para logs y debugging)
    @Override public String toString() {
        return "Pedido{id=" + idPedido + ", estado=" + estado + ", total=" + total +
               ", cliente=" + (cliente != null ? cliente.getNombreCompleto() : "no cargado") +
               ", detalles=" + (detalles != null ? detalles.size() + " items" : "no cargados") +
               ", pago="     + (pago     != null ? pago.getEstadoPago()       : "sin pago") +
               ", envio="    + (envio    != null ? envio.getEstadoEntrega()   : "sin envio") + '}';
    }
}
