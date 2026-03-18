
package logica;

import enums.EstadoEntrega; // enum con estados: PREPARANDO, EN_TRANSITO, ENTREGADO, DEVUELTO
import enums.EstadoPedido;  // enum para sincronizar el estado del pedido al cambiar el envío
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
 * Envio — Entidad JPA que mapea la tabla "envio".
 *
 * Representa el despacho físico de un pedido a un cliente.
 * Cada pedido puede tener como máximo un envío (relación 1:1 con Pedido).
 * Registra la transportadora, número de guía, dirección y fechas de despacho/entrega.
 * Al crearse, cambia automáticamente el estado del pedido a ENVIADO.
 * Al confirmarse la entrega, cambia el estado del pedido a ENTREGADO.
 * Es gestionado desde el panel admin mediante SvEnvios.java.
 */
@Entity                  // esta clase es una entidad JPA (mapea una tabla)
@Table(name = "envio")   // nombre de la tabla en MySQL
public class Envio {

    @Id                                                    // clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY)    // AUTO_INCREMENT en MySQL
    @Column(name = "id_envio")                             // columna id_envio en la tabla
    private int idEnvio;

    @OneToOne                                              // relación 1:1 — un envío pertenece a un solo pedido
    @JoinColumn(name = "id_pedido", nullable = false)      // FK al pedido, no puede ser nula
    private Pedido pedido;

    @Column(name = "fecha_envio")                          // fecha y hora en que se despacha el paquete
    private LocalDateTime fechaEnvio;

    @Column(name = "fecha_estimada_entrega")               // fecha en que se espera llegue al cliente
    private LocalDateTime fechaEstimadaEntrega;

    @Enumerated(EnumType.STRING)                           // guarda el nombre del enum como texto en BD (no como número)
    @Column(name = "estado_entrega", nullable = false, length = 20) // ej: "PREPARANDO", "EN_TRANSITO", "ENTREGADO"
    private EstadoEntrega estadoEntrega;

    @Column(name = "created_at")                           // fecha de creación del registro
    private LocalDateTime createdAt;

    @Column(name = "updated_at")                           // fecha de última modificación
    private LocalDateTime updatedAt;

    @Column(name = "direccion_envio", length = 255)        // dirección física a donde se envía el pedido
    private String direccionEnvio;

    @Column(name = "transportadora", length = 100)         // empresa de mensajería (ej: Servientrega, Coordinadora)
    private String transportadora;

    @Column(name = "numero_guia", length = 100)            // número de guía para rastreo del paquete
    private String numeroGuia;

    @Column(name = "activo", nullable = false)             // false = envío cancelado o eliminado lógicamente
    private boolean activo;

    public Envio() {
    } // constructor vacío requerido por JPA

    // Constructor completo para crear un envío con todos sus datos
    public Envio(int idEnvio, Pedido pedido, LocalDateTime fechaEnvio, LocalDateTime fechaEstimadaEntrega, EstadoEntrega estadoEntrega, LocalDateTime createdAt, LocalDateTime updatedAt, String direccionEnvio, String transportadora, String numeroGuia, boolean activo) {
        this.idEnvio = idEnvio;
        this.pedido = pedido;
        this.fechaEnvio = fechaEnvio;
        this.fechaEstimadaEntrega = fechaEstimadaEntrega;
        this.estadoEntrega = estadoEntrega;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.direccionEnvio = direccionEnvio;
        this.transportadora = transportadora;
        this.numeroGuia = numeroGuia;
        this.activo = activo;
    }
    
    // Método de negocio: marca el envío como despachado y sincroniza el estado del pedido
    // Registra la guía, la fecha actual de despacho y cambia el estado a EN_TRANSITO
    // También actualiza el pedido asociado a ENVIADO automáticamente
    public void despachar(String numeroGuia) {
        this.numeroGuia = numeroGuia;                          // guarda el número de guía de la transportadora
        this.fechaEnvio = LocalDateTime.now();                // registra la fecha/hora exacta del despacho
        this.estadoEntrega = EstadoEntrega.EN_TRANSITO;       // cambia estado del envío a EN_TRANSITO
        this.updatedAt = LocalDateTime.now();                 // actualiza timestamp de modificación
        if (pedido != null) pedido.setEstado(EstadoPedido.ENVIADO); // sincroniza el estado del pedido padre
    }

    // Método de negocio: confirma que el cliente recibió el paquete
    // Cambia el estado del envío a ENTREGADO y actualiza el pedido a ENTREGADO
    public void confirmarEntrega() {
        this.estadoEntrega = EstadoEntrega.ENTREGADO;          // estado final exitoso del envío
        this.updatedAt = LocalDateTime.now();                  // actualiza timestamp
        if (pedido != null) pedido.setEstado(EstadoPedido.ENTREGADO); // sincroniza el pedido padre
    }

    // Método de negocio: registra que el paquete fue devuelto por el cliente o no fue recibido
    public void registrarDevolucion() { this.estadoEntrega = EstadoEntrega.DEVUELTO; this.updatedAt = LocalDateTime.now(); }

    public int getIdEnvio() {
        return idEnvio;
    }

    public void setIdEnvio(int idEnvio) {
        this.idEnvio = idEnvio;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public LocalDateTime getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(LocalDateTime fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }

    public LocalDateTime getFechaEstimadaEntrega() {
        return fechaEstimadaEntrega;
    }

    public void setFechaEstimadaEntrega(LocalDateTime fechaEstimadaEntrega) {
        this.fechaEstimadaEntrega = fechaEstimadaEntrega;
    }

    public EstadoEntrega getEstadoEntrega() {
        return estadoEntrega;
    }

    public void setEstadoEntrega(EstadoEntrega estadoEntrega) {
        this.estadoEntrega = estadoEntrega;
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

    public String getDireccionEnvio() {
        return direccionEnvio;
    }

    public void setDireccionEnvio(String direccionEnvio) {
        this.direccionEnvio = direccionEnvio;
    }

    public String getTransportadora() {
        return transportadora;
    }

    public void setTransportadora(String transportadora) {
        this.transportadora = transportadora;
    }

    public String getNumeroGuia() {
        return numeroGuia;
    }

    public void setNumeroGuia(String numeroGuia) {
        this.numeroGuia = numeroGuia;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return "Envio{" + "idEnvio=" + idEnvio + ", pedido=" + 
            (pedido != null ? pedido.getIdPedido() : "no cargado") + ", estadoEntrega=" + estadoEntrega + ", transportadora=" + transportadora + '}';
    }
   
}

