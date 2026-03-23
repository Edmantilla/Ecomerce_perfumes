
package logica;

import exceptions.ValidacionException;       // excepción para datos inválidos
import java.time.LocalDateTime;              // manejo de fechas con hora
import javax.persistence.Column;             // mapeo de columnas
import javax.persistence.Entity;             // marca esta clase como entidad JPA
import javax.persistence.GeneratedValue;     // generación automática del ID
import javax.persistence.GenerationType;     // estrategia IDENTITY = autoincrement
import javax.persistence.Id;                 // clave primaria
import javax.persistence.JoinColumn;         // columna FK
import javax.persistence.ManyToOne;          // relación muchos-a-uno
import javax.persistence.OneToOne;           // relación uno-a-uno
import javax.persistence.Table;              // nombre de la tabla en MySQL

/**
 * Usuario — Entidad JPA que representa una cuenta de acceso al sistema.
 * Mapea la tabla 'usuario' de MySQL.
 * Contiene las credenciales (correo + contraseña) y las relaciones con:
 *   - Cliente (1:1, opcional): si id_cliente es NULL → admin puro (sin datos de cliente)
 *   - Rol (N:1): determina los permisos del usuario
 * Método clave: esAdmin() devuelve true si el usuario NO tiene cliente asociado.
 * Las contraseñas se almacenan en TEXTO PLANO (sin hash).
 */
@Entity                          // esta clase es una entidad JPA
@Table(name = "usuario")         // tabla MySQL: 'usuario'
public class Usuario {

    @Id                                                       // clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY)        // autoincrement en MySQL
    @Column(name = "id_usuario")                               // columna: id_usuario
    private int idUsuario;                                     // ID único de la cuenta

    // Relación 1:1 con Cliente (OPCIONAL: NULL = admin puro sin datos de cliente)
    @OneToOne                                                  // un usuario = un cliente
    @JoinColumn(name = "id_cliente")                           // FK: id_cliente (nullable)
    private Cliente cliente;                                   // datos personales del cliente (null si es admin puro)

    // Relación N:1 con Rol (muchos usuarios comparten un mismo rol)
    @ManyToOne                                                 // muchos usuarios → un rol
    @JoinColumn(name = "id_rol", nullable = false)             // FK: id_rol, obligatorio
    private Rol rol;                                           // rol del usuario (ADMIN, CLIENTE, VENDEDOR, etc.)

    @Column(name = "ultimo_acceso")                            // columna: ultimo_acceso
    private LocalDateTime ultimoAcceso;                        // fecha/hora del último login exitoso

    @Column(name = "created_at")                               // columna: created_at
    private LocalDateTime createdAt;                           // fecha de creación de la cuenta

    @Column(name = "updated_at")                               // columna: updated_at
    private LocalDateTime updatedAt;                           // última actualización

    @Column(name = "correo_usuario", nullable = false, length = 200, unique = true) // obligatorio, único
    private String correoUsuario;                              // correo electrónico (login)

    @Column(name = "contrasena", nullable = false)             // obligatorio
    private String contrasena;                                 // contraseña en texto plano (sin hash)

    @Column(name = "activo", nullable = false)                 // obligatorio
    private boolean activo;                                    // true=puede iniciar sesión, false=bloqueado

    // Constructor vacío requerido por JPA
    public Usuario() {
    }

    // Constructor completo con todos los campos
    public Usuario(int idUsuario, Cliente cliente, Rol rol, LocalDateTime ultimoAcceso, LocalDateTime createdAt, LocalDateTime updatedAt, String correoUsuario, String contrasena, boolean activo) {
        this.idUsuario = idUsuario;
        this.cliente = cliente;
        this.rol = rol;
        this.ultimoAcceso = ultimoAcceso;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.correoUsuario = correoUsuario;
        this.contrasena = contrasena;
        this.activo = activo;
    }
    
    // Devuelve true si el usuario es admin puro (NO tiene cliente asociado)
    // Admin puro = usuario creado directamente en BD con id_cliente=NULL
    public boolean esAdmin(){ return this.cliente == null; }

    // Verifica si el usuario tiene un permiso específico a través de su rol
    // Delega al método tienePermiso() de la clase Rol
    public boolean tienePermiso(String nombrePermiso) {
        if (rol == null) return false;         // sin rol = sin permisos
        return rol.tienePermiso(nombrePermiso); // busca el permiso en los permisos del rol
    }

    // Actualiza la fecha de último acceso y la fecha de modificación
    // Se llama desde SvLogin tras un login exitoso
    public void registrarAcceso() {
        this.ultimoAcceso = LocalDateTime.now();
        this.updatedAt    = LocalDateTime.now();
    }

    
    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public LocalDateTime getUltimoAcceso() {
        return ultimoAcceso;
    }

    public void setUltimoAcceso(LocalDateTime ultimoAcceso) {
        this.ultimoAcceso = ultimoAcceso;
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

    public String getCorreoUsuario() {
        return correoUsuario;
    }

    // Setter con validación: correo obligatorio, formato válido de email
    // Se convierte a minúsculas y se recortan espacios
    public void setCorreoUsuario(String correoUsuario) {
        if (correoUsuario == null || correoUsuario.isBlank())
            throw new ValidacionException("El correo del usuario no puede estar vacío.");
        if (!correoUsuario.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) // regex: formato email básico
            throw new ValidacionException("Formato de correo de usuario inválido.");
        this.correoUsuario = correoUsuario.toLowerCase().trim(); // normaliza a minúsculas
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    // Representación en texto del usuario (para logs y debugging)
    @Override
    public String toString() {
        return "Usuario{" + "idUsuario=" + idUsuario + ", rol=" + (rol != null ? rol.getNombreRol() : "no cargado") +  ", esAdmin=" + esAdmin() + ", correoUsuario=" + correoUsuario + '}';
    }
    

    
}
