
package persistencias;

import javax.persistence.EntityManagerFactory; // fábrica de conexiones JPA

/**
 * ControladoraPersistencia — Clase puente que centraliza todas las instancias
 * de los JpaControllers del proyecto.
 * 
 * Cada JpaController maneja las operaciones CRUD de una entidad específica.
 * Esta clase los agrupa para que la capa de lógica (Controladora) tenga
 * un único punto de acceso a la persistencia.
 * 
 * Actualmente la clase Controladora apenas la usa; los servlets crean
 * directamente sus propios JpaControllers. Esta clase se mantiene como
 * parte de la arquitectura de capas original del proyecto.
 */
public class ControladoraPersistencia {
    
    // Obtiene la fábrica de EntityManagers del Singleton JpaProvider
    private final EntityManagerFactory emf = JpaProvider.getEntityManagerFactory();

    // Instancias de cada JpaController, una por entidad JPA del proyecto
    // Cada una recibe el EMF compartido para crear sus propios EntityManagers
    CategoriaJpaController categoriaJPA = new CategoriaJpaController(emf);        // CRUD para Categoria
    ClienteJpaController clienteJPA = new ClienteJpaController(emf);              // CRUD para Cliente
    CorreoclienteJpaController correoJPA = new CorreoclienteJpaController(emf);   // CRUD para Correocliente
    DetallepedidoJpaController detallepedidoJPA = new DetallepedidoJpaController(emf); // CRUD para Detallepedido
    EnvioJpaController envioJPA = new EnvioJpaController(emf);                    // CRUD para Envio
    MarcaJpaController marcaJPA = new MarcaJpaController(emf);                    // CRUD para Marca
    PagoJpaController pagoJPA = new PagoJpaController(emf);                       // CRUD para Pago
    PedidoJpaController pedidoJPA = new PedidoJpaController(emf);                 // CRUD para Pedido
    PermisoJpaController permisoJPA = new PermisoJpaController(emf);              // CRUD para Permiso
    ProductoJpaController productoJPA = new ProductoJpaController(emf);            // CRUD para Producto
    RolJpaController rolJPA = new RolJpaController(emf);                          // CRUD para Rol
    RolpermisoJpaController rolpermisoJPA = new RolpermisoJpaController(emf);     // CRUD para Rolpermiso
    TelefonoclienteJpaController telefonoJPA = new TelefonoclienteJpaController(emf); // CRUD para Telefonocliente
    UsuarioJpaController usuarioJPA = new UsuarioJpaController(emf);              // CRUD para Usuario
}
