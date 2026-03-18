
package logica;

import exceptions.ValidacionException; // excepción personalizada para errores de validación
import enums.TipoTelefono;             // enum: CELULAR, FIJO, TRABAJO, etc.
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
 * Telefonocliente — Entidad JPA que mapea la tabla "telefono_cliente".
 *
 * Un cliente puede tener múltiples números de teléfono registrados.
 * Cada teléfono tiene un tipo (CELULAR, FIJO, TRABAJO, etc.) definido por el enum TipoTelefono.
 * Los teléfonos inactivos (activo=false) no se muestran en el perfil del usuario.
 * Es gestionada desde perfil.jsp mediante SvContactoCliente.
 * El administrador puede ver los teléfonos de cada cliente en el panel de usuarios.
 */
@Entity                            // esta clase es una entidad JPA (mapea una tabla)
@Table(name = "telefono_cliente")   // nombre de la tabla en MySQL
public class Telefonocliente {

    @Id                                                    // clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY)    // AUTO_INCREMENT en MySQL
    @Column(name = "id_telefono")                          // columna id_telefono en la tabla
    private int idTelefono;

    @ManyToOne                                             // relación N:1 — muchos telïonos pertenecen a un cliente
    @JoinColumn(name = "id_cliente", nullable = false)     // FK al cliente, no puede ser nula
    private Cliente cliente;

    @Enumerated(EnumType.STRING)                           // guarda el nombre del enum como texto en BD (no como número)
    @Column(name = "tipo_telefono", nullable = false, length = 20) // ej: "CELULAR", "FIJO", "TRABAJO"
    private TipoTelefono tipoTelefono;

    @Column(name = "telefono", nullable = false, length = 20) // número de teléfono, obligatorio, máx 20 chars
    private String telefono;

    @Column(name = "activo", nullable = false)             // false = teléfono eliminado lógicamente
    private boolean activo;

    public Telefonocliente() {
    } // constructor vacío requerido por JPA

    // Constructor completo para crear un teléfono con todos sus datos
    public Telefonocliente(int idTelefono, Cliente cliente, TipoTelefono tipoTelefono, String telefono, boolean activo) {
        this.idTelefono = idTelefono;
        this.cliente = cliente;
        this.tipoTelefono = tipoTelefono;
        this.telefono = telefono;
        this.activo = activo;
    }

    // Retorna el ID del teléfono (clave primaria)
    public int getIdTelefono() {
        return idTelefono;
    }

    // Asigna el ID (normalmente solo lo usa JPA internamente)
    public void setIdTelefono(int idTelefono) {
        this.idTelefono = idTelefono;
    }

    // Retorna el cliente dueño de este teléfono
    public Cliente getCliente() {
        return cliente;
    }

    // Vincula este teléfono con su cliente propietario
    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    // Retorna el tipo de teléfono (CELULAR, FIJO, TRABAJO...)
    public TipoTelefono getTipoTelefono() {
        return tipoTelefono;
    }

    // Asigna el tipo con validación: no puede ser null
    public void setTipoTelefono(TipoTelefono tipoTelefono) {
        if (tipoTelefono == null) throw new ValidacionException("El tipo de teléfono es obligatorio."); // el tipo es obligatorio
        this.tipoTelefono = tipoTelefono;
    }

    // Retorna el número de teléfono
    public String getTelefono() {
        return telefono;
    }

    // Asigna el teléfono con validación de formato:
    public void setTelefono(String telefono) {
        if (telefono == null || telefono.isBlank())
            throw new ValidacionException("El telefono no puede estar vacio.");   // no puede ser vacío
        String limpio = telefono.replaceAll("[\\s\\-\\(\\)\\+]", ""); // eliminar espacios, guiones, paréntesis y +
        if (!limpio.matches("\\d{7,15}"))                              // después de limpiar debe tener entre 7 y 15 dígitos
            throw new ValidacionException("Formato de telefono invalido: " + telefono);
        this.telefono = telefono; // se guarda el formato original (con guiones o espacios si el usuario los ingresó)
    }

    // Retorna si el teléfono está activo (visible en el perfil)
    public boolean isActivo() {
        return activo;
    }

    // Activa o desactiva (eliminación lógica) este teléfono
    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    // Representación en texto para logs y depuración
    // Muestra el ID del cliente en vez del objeto completo para evitar referencias circulares
    @Override
    public String toString() {
        return "Telefonocliente{" + "idTelefono=" + idTelefono + ", cliente=" + 
                (cliente != null ? cliente.getIdCliente() : "no cargado") + ", tipoTelefono=" + tipoTelefono + ", telefono=" + telefono + '}';
    }
    
}
