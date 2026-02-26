
package logica;

import exceptions.ValidacionException;
import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 *
 * @author eduar
 */
@Entity
@Table(name = "cliente")
public class Cliente {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private int idCliente;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "nombre_completo", nullable = false, length = 150)
    private String nombreCompleto;

    @Column(name = "direccion", nullable = false)
    private String direccion;

    @Column(name = "activo", nullable = false)
    private boolean activo;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL)
    private List<Telefonocliente> telefonos;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL)
    private List<Correocliente> correos;

    @OneToMany(mappedBy = "cliente")
    private List<Pedido> pedidos;
    
    public Cliente() {
    }

    public Cliente(int idCliente, LocalDateTime createdAt, LocalDateTime updatedAt, String nombreCompleto, String direccion, boolean activo, List<Telefonocliente> telefonos, List<Correocliente> correos, List<Pedido> pedidos) {
        this.idCliente = idCliente;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.nombreCompleto = nombreCompleto;
        this.direccion = direccion;
        this.activo = activo;
        this.telefonos = telefonos;
        this.correos = correos;
        this.pedidos = pedidos;
    }

    public String getCorreoPrincipal() {
       if (correos == null) return null;
       return correos.stream()
            .filter(c -> c.isPrincipal() && c.isActivo())
            .map(Correocliente::getCorreo)
            .findFirst().orElse(null);
    }

    public boolean tieneCorreosCargados()   { return correos   != null; }
    public boolean tieneTelefonosCargados() { return telefonos != null; }
    public boolean tienePedidosCargados()   { return pedidos   != null; }

    
    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
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

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
         if (nombreCompleto == null || nombreCompleto.isBlank())
            throw new ValidacionException("El nombre del cliente no puede estar vacío.");
        if (nombreCompleto.length() > 150)
            throw new ValidacionException("El nombre no puede superar 150 caracteres.");
        this.nombreCompleto = nombreCompleto.trim();
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        if (direccion == null || direccion.isBlank())
            throw new ValidacionException("La dirección no puede estar vacía.");
        this.direccion = direccion;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public List<Telefonocliente> getTelefonos() {
        return telefonos;
    }

    public void setTelefonos(List<Telefonocliente> telefonos) {
        this.telefonos = telefonos;
    }

    public List<Correocliente> getCorreos() {
        return correos;
    }

    public void setCorreos(List<Correocliente> correos) {
        this.correos = correos;
    }

    public List<Pedido> getPedidos() {
        return pedidos;
    }

    public void setPedidos(List<Pedido> pedidos) {
        this.pedidos = pedidos;
    }

    @Override
    public String toString() {
        return "Cliente{" + "idCliente=" + idCliente + ", nombreCompleto=" + nombreCompleto + ", activo=" + activo + 
                ", telefonos=" + (telefonos != null ? telefonos.size() + " cargados" : "no cargados") + 
                ", correos=" + (correos   != null ? correos.size()   + " cargados" : "no cargados") + 
                ", pedidos=" + (pedidos   != null ? pedidos.size()   + " cargados" : "no cargados") + '}';
    }

}
