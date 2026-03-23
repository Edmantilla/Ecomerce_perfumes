
package logica;

import exceptions.ValidacionException;       // excepción para validaciones de datos
import java.time.LocalDateTime;              // manejo de fechas con hora
import java.util.List;                       // listas de relaciones (teléfonos, correos, pedidos)
import javax.persistence.CascadeType;        // propagación de operaciones JPA en cascada
import javax.persistence.Column;             // mapeo de columnas de la tabla
import javax.persistence.Entity;             // marca esta clase como entidad JPA
import javax.persistence.GeneratedValue;     // generación automática del ID
import javax.persistence.GenerationType;     // estrategia de generación (IDENTITY = autoincrement)
import javax.persistence.Id;                 // marca el campo como clave primaria
import javax.persistence.OneToMany;          // relación uno-a-muchos
import javax.persistence.Table;              // nombre de la tabla en MySQL

/**
 * Cliente — Entidad JPA que representa un cliente registrado en la tienda.
 * Mapea la tabla 'cliente' de MySQL.
 * Contiene los datos personales del cliente: nombre, dirección y estado.
 * Tiene relaciones uno-a-muchos con: Telefonocliente, Correocliente, Pedido.
 * Se vincula 1:1 con Usuario (un cliente tiene una cuenta de usuario).
 */
@Entity                          // esta clase es una entidad JPA (mapea una tabla)
@Table(name = "cliente")         // tabla MySQL que representa: 'cliente'
public class Cliente {
    
    @Id                                                       // clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY)        // autoincrement en MySQL
    @Column(name = "id_cliente")                               // columna: id_cliente
    private int idCliente;                                     // ID único del cliente

    @Column(name = "created_at")                               // columna: created_at
    private LocalDateTime createdAt;                           // fecha de creación del registro

    @Column(name = "updated_at")                               // columna: updated_at
    private LocalDateTime updatedAt;                           // última actualización del registro

    @Column(name = "nombre_completo", nullable = false, length = 150) // obligatorio, máx 150 chars
    private String nombreCompleto;                             // nombre completo del cliente

    @Column(name = "direccion", nullable = false)              // obligatorio
    private String direccion;                                  // dirección de envío registrada

    @Column(name = "activo", nullable = false)                 // obligatorio
    private boolean activo;                                    // true=activo, false=desactivado

    // Relación 1:N con Telefonocliente. CascadeType.ALL: si se guarda/elimina el cliente,
    // también se guardan/eliminan sus teléfonos. mappedBy="cliente" = el FK está en Telefonocliente.
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL)
    private List<Telefonocliente> telefonos;                   // teléfonos del cliente

    // Relación 1:N con Correocliente. CascadeType.ALL = propagación en cascada.
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL)
    private List<Correocliente> correos;                       // correos adicionales del cliente

    // Relación 1:N con Pedido. Sin cascada: los pedidos se gestionan independientemente.
    @OneToMany(mappedBy = "cliente")
    private List<Pedido> pedidos;                              // pedidos realizados por el cliente
    
    // Constructor vacío requerido por JPA para crear instancias vía reflexión
    public Cliente() {
    }

    // Constructor completo con todos los campos (usado para crear clientes programáticamente)
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

    // Obtiene el correo marcado como principal y activo del cliente
    // Usa Streams para filtrar la lista de correos y devolver el primero que cumpla
    // Devuelve null si no tiene correos cargados o ninguno es principal+activo
    public String getCorreoPrincipal() {
       if (correos == null) return null;
       return correos.stream()
            .filter(c -> c.isPrincipal() && c.isActivo()) // solo principal y activo
            .map(Correocliente::getCorreo)                // extrae el string del correo
            .findFirst().orElse(null);                    // primer resultado o null
    }

    // Métodos para verificar si las colecciones lazy ya fueron cargadas por JPA
    // Útiles para evitar LazyInitializationException al acceder fuera del EntityManager
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

    // Setter con validación: el nombre no puede ser nulo, vacío ni mayor a 150 chars
    public void setNombreCompleto(String nombreCompleto) {
         if (nombreCompleto == null || nombreCompleto.isBlank())
            throw new ValidacionException("El nombre del cliente no puede estar vacío.");
        if (nombreCompleto.length() > 150)
            throw new ValidacionException("El nombre no puede superar 150 caracteres.");
        this.nombreCompleto = nombreCompleto.trim(); // elimina espacios al inicio y final
    }

    public String getDireccion() {
        return direccion;
    }

    // Setter con validación: la dirección no puede ser nula ni vacía
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

    // Representación en texto del cliente (para logs y debugging)
    // Muestra si las colecciones lazy están cargadas o no para evitar accesos accidentales
    @Override
    public String toString() {
        return "Cliente{" + "idCliente=" + idCliente + ", nombreCompleto=" + nombreCompleto + ", activo=" + activo + 
                ", telefonos=" + (telefonos != null ? telefonos.size() + " cargados" : "no cargados") + 
                ", correos=" + (correos   != null ? correos.size()   + " cargados" : "no cargados") + 
                ", pedidos=" + (pedidos   != null ? pedidos.size()   + " cargados" : "no cargados") + '}';
    }

}
