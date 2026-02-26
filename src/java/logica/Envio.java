
package logica;

import enums.EstadoEntrega;
import enums.EstadoPedido;
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
@Table(name = "envio")
public class Envio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_envio")
    private int idEnvio;

    @OneToOne
    @JoinColumn(name = "id_pedido", nullable = false)
    private Pedido pedido;

    @Column(name = "fecha_envio")
    private LocalDateTime fechaEnvio;

    @Column(name = "fecha_estimada_entrega")
    private LocalDateTime fechaEstimadaEntrega;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_entrega", nullable = false, length = 20)
    private EstadoEntrega estadoEntrega;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "direccion_envio", length = 255)
    private String direccionEnvio;

    @Column(name = "transportadora", length = 100)
    private String transportadora;

    @Column(name = "numero_guia", length = 100)
    private String numeroGuia;

    @Column(name = "activo", nullable = false)
    private boolean activo;

    public Envio() {
    }

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
    
    
    
    public void despachar(String numeroGuia) {
        this.numeroGuia = numeroGuia; this.fechaEnvio = LocalDateTime.now();
        this.estadoEntrega = EstadoEntrega.EN_TRANSITO; this.updatedAt = LocalDateTime.now();
        if (pedido != null) pedido.setEstado(EstadoPedido.ENVIADO);
    }
    public void confirmarEntrega() {
        this.estadoEntrega = EstadoEntrega.ENTREGADO; this.updatedAt = LocalDateTime.now();
        if (pedido != null) pedido.setEstado(EstadoPedido.ENTREGADO);
    }
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

