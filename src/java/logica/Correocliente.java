
package logica;

import exceptions.ValidacionException; // excepción personalizada para errores de validación
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Correocliente — Entidad JPA que mapea la tabla "correo_cliente".
 *
 * Un cliente puede tener varios correos electrónicos registrados.
 * El correo marcado como "principal" es el que se usa para notificaciones.
 * Los correos inactivos (activo=false) no se muestran en el perfil del usuario.
 * Esta entidad es gestionada desde perfil.jsp mediante SvContactoCliente.
 */
@Entity                          // le dice a JPA que esta clase mapea una tabla de BD
@Table(name = "correo_cliente")  // nombre exacto de la tabla en MySQL
public class Correocliente {
    
    @Id                                                    // clave primaria de la tabla
    @GeneratedValue(strategy = GenerationType.IDENTITY)    // AUTO_INCREMENT en MySQL
    @Column(name = "id_correo")                            // columna id_correo en la tabla
    private int idCorreo;

    @ManyToOne                                             // relación N:1 — muchos correos pertenecen a un cliente
    @JoinColumn(name = "id_cliente", nullable = false)     // FK id_cliente, no puede ser nula
    private Cliente cliente;

    @Column(name = "correo", nullable = false, length = 200) // el correo electrónico, obligatorio
    private String correo;

    @Column(name = "principal", nullable = false)          // true = correo principal del cliente
    private boolean principal;

    @Column(name = "activo", nullable = false)             // false = correo eliminado lógicamente
    private boolean activo;

    public Correocliente() {
    } // constructor vacío requerido por JPA

    // Constructor completo para crear un correo con todos sus datos
    public Correocliente(int idCorreo, Cliente cliente, String correo, boolean principal, boolean activo) {
        this.idCorreo = idCorreo;
        this.cliente = cliente;
        this.correo = correo;
        this.principal = principal;
        this.activo = activo;
    }

    // Retorna el ID del correo (clave primaria)
    public int getIdCorreo() {
        return idCorreo;
    }

    // Asigna el ID (normalmente solo lo usa JPA internamente)
    public void setIdCorreo(int idCorreo) {
        this.idCorreo = idCorreo;
    }

    // Retorna el cliente dueño de este correo
    public Cliente getCliente() {
        return cliente;
    }

    // Vincula este correo con su cliente propietario
    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    // Retorna la dirección de correo electrónico
    public String getCorreo() {
        return correo;
    }

    // Asigna la dirección de correo con validaciones de formato:
    public void setCorreo(String correo) {
        if (correo == null || correo.isBlank())                            // no puede ser vacío
            throw new ValidacionException("El correo no puede estar vacío.");
        if (!correo.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$"))         // debe tener formato email válido
            throw new ValidacionException("Formato de correo inválido: " + correo);
        this.correo = correo;
    }

    // Retorna si este es el correo principal del cliente
    public boolean isPrincipal() {
        return principal;
    }

    // Marca o desmarca este correo como principal
    public void setPrincipal(boolean principal) {
        this.principal = principal;
    }

    // Retorna si el correo está activo (visible en el perfil)
    public boolean isActivo() {
        return activo;
    }

    // Activa o desactiva (eliminación lógica) este correo
    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    // Representación en texto para logs y depuración
    // Muestra el ID del cliente en vez del objeto completo para evitar referencias circulares
    @Override
    public String toString() {
        return "Correocliente{" + "idCorreo=" + idCorreo + ", cliente=" + 
                (cliente != null ? cliente.getIdCliente() : "no cargado") + ", correo=" + correo + ", principal=" + principal + ", activo=" + activo + '}';
    }
    

    
}
