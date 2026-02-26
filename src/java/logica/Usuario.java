
package logica;

import exceptions.ValidacionException;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 *
 * @author eduar
 */
@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private int idUsuario;

    @OneToOne
    @JoinColumn(name = "id_cliente")
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "id_rol", nullable = false)
    private Rol rol;

    @Column(name = "ultimo_acceso")
    private LocalDateTime ultimoAcceso;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "correo_usuario", nullable = false, length = 200, unique = true)
    private String correoUsuario;

    @Column(name = "contrasena", nullable = false)
    private String contrasena;

    @Column(name = "activo", nullable = false)
    private boolean activo;

    public Usuario() {
    }

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
    
    public boolean esAdmin(){ return this.cliente == null; }

    public boolean tienePermiso(String nombrePermiso) {
        if (rol == null) return false;
        return rol.tienePermiso(nombrePermiso);
    }

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

    public void setCorreoUsuario(String correoUsuario) {
        if (correoUsuario == null || correoUsuario.isBlank())
            throw new ValidacionException("El correo del usuario no puede estar vacío.");
        if (!correoUsuario.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$"))
            throw new ValidacionException("Formato de correo de usuario inválido.");
        this.correoUsuario = correoUsuario.toLowerCase().trim();
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

    @Override
    public String toString() {
        return "Usuario{" + "idUsuario=" + idUsuario + ", rol=" + (rol != null ? rol.getNombreRol() : "no cargado") +  ", esAdmin=" + esAdmin() + ", correoUsuario=" + correoUsuario + '}';
    }
    

    
}
