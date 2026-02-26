
package logica;

import exceptions.ValidacionException;
import javax.persistence.Column;
import javax.persistence.Entity;
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
@Table(name = "correo_cliente")
public class Correocliente {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_correo")
    private int idCorreo;

    @ManyToOne
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @Column(name = "correo", nullable = false, length = 200)
    private String correo;

    @Column(name = "principal", nullable = false)
    private boolean principal;

    @Column(name = "activo", nullable = false)
    private boolean activo;

    public Correocliente() {
    }

    public Correocliente(int idCorreo, Cliente cliente, String correo, boolean principal, boolean activo) {
        this.idCorreo = idCorreo;
        this.cliente = cliente;
        this.correo = correo;
        this.principal = principal;
        this.activo = activo;
    }

    public int getIdCorreo() {
        return idCorreo;
    }

    public void setIdCorreo(int idCorreo) {
        this.idCorreo = idCorreo;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        if (correo == null || correo.isBlank())
            throw new ValidacionException("El correo no puede estar vacío.");
        if (!correo.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$"))
            throw new ValidacionException("Formato de correo inválido: " + correo);
        this.correo = correo;
    }

    public boolean isPrincipal() {
        return principal;
    }

    public void setPrincipal(boolean principal) {
        this.principal = principal;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return "Correocliente{" + "idCorreo=" + idCorreo + ", cliente=" + 
                (cliente != null ? cliente.getIdCliente() : "no cargado") + ", correo=" + correo + ", principal=" + principal + ", activo=" + activo + '}';
    }
    

    
}
