
package persistencias;

import javax.persistence.EntityManagerFactory;
/**
 *
 * @author eduar
 */
public class ControladoraPersistencia {
    
    private final EntityManagerFactory emf = JpaProvider.getEntityManagerFactory();

    CategoriaJpaController categoriaJPA = new CategoriaJpaController(emf);
    ClienteJpaController clienteJPA = new ClienteJpaController(emf);
    CorreoclienteJpaController correoJPA = new CorreoclienteJpaController(emf);
    DetallepedidoJpaController detallepedidoJPA = new DetallepedidoJpaController(emf);
    EnvioJpaController envioJPA = new EnvioJpaController(emf);
    MarcaJpaController marcaJPA = new MarcaJpaController(emf);
    PagoJpaController pagoJPA = new PagoJpaController(emf);
    PedidoJpaController pedidoJPA = new PedidoJpaController(emf);
    PermisoJpaController permisoJPA = new PermisoJpaController(emf);
    ProductoJpaController productoJPA = new ProductoJpaController(emf);
    RolJpaController rolJPA = new RolJpaController(emf);
    RolpermisoJpaController rolpermisoJPA = new RolpermisoJpaController(emf);
    TelefonoclienteJpaController telefonoJPA = new TelefonoclienteJpaController(emf);
    UsuarioJpaController usuarioJPA = new UsuarioJpaController(emf);
}
