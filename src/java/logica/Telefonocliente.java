
package logica;

import exceptions.ValidacionException;
import enums.TipoTelefono;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 *
 * @author eduar
 */
@Entity
@Table(name = "telefono_cliente")
public class Telefonocliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_telefono")
    private int idTelefono;

    @ManyToOne
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_telefono", nullable = false, length = 20)
    private TipoTelefono tipoTelefono;

    @Column(name = "telefono", nullable = false, length = 20)
    private String telefono;

    @Column(name = "activo", nullable = false)
    private boolean activo;

    public Telefonocliente() {
    }

    public Telefonocliente(int idTelefono, Cliente cliente, TipoTelefono tipoTelefono, String telefono, boolean activo) {
        this.idTelefono = idTelefono;
        this.cliente = cliente;
        this.tipoTelefono = tipoTelefono;
        this.telefono = telefono;
        this.activo = activo;
    }

    public int getIdTelefono() {
        return idTelefono;
    }

    public void setIdTelefono(int idTelefono) {
        this.idTelefono = idTelefono;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public TipoTelefono getTipoTelefono() {
        return tipoTelefono;
    }

    public void setTipoTelefono(TipoTelefono tipoTelefono) {
        if (tipoTelefono == null) throw new ValidacionException("El tipo de teléfono es obligatorio.");
        this.tipoTelefono = tipoTelefono;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        if (telefono == null || telefono.isBlank())
            throw new ValidacionException("El telefono no puede estar vacio.");
        String limpio = telefono.replaceAll("[\\s\\-\\(\\)\\+]", "");
        if (limpio.matches("\\d{7,15}"))
            throw  new ValidacionException("Formato de telefono invalido: "+ telefono);
        this.telefono = telefono;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return "Telefonocliente{" + "idTelefono=" + idTelefono + ", cliente=" + 
                (cliente != null ? cliente.getIdCliente() : "no cargado") + ", tipoTelefono=" + tipoTelefono + ", telefono=" + telefono + '}';
    }
    
}
