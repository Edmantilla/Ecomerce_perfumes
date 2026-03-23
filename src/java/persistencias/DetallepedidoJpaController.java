package persistencias;

import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import logica.Detallepedido;
import logica.Pedido;
import logica.Producto;
import persistencias.exceptions.NonexistentEntityException;

/**
 * DetallepedidoJpaController — CRUD para la entidad Detallepedido (tabla 'detalle_pedido').
 * Provee: create, edit, destroy, findDetallepedidoEntities, findDetallepedido, getDetallepedidoCount.
 * Usado por SvCompra para crear las líneas de detalle al hacer checkout.
 * Resuelve FK con Pedido y Producto mediante em.getReference().
 */
public class DetallepedidoJpaController implements Serializable {

    // Constructor con EMF explícito
    public DetallepedidoJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    // Crea un nuevo EntityManager (conexión activa a BD)
    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }
    
    // Constructor sin parámetros: usa EMF del Singleton JpaProvider
    public DetallepedidoJpaController() {
        this(JpaProvider.getEntityManagerFactory());
    }

    // CREATE: INSERT INTO detalle_pedido (...) VALUES (...)
    // Resuelve FK con Pedido y Producto mediante getReference()
    public void create(Detallepedido detallepedido) throws Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Pedido pedido = detallepedido.getPedido();
            if (pedido != null) {
                pedido = em.getReference(pedido.getClass(), pedido.getIdPedido());
                detallepedido.setPedido(pedido);
            }
            Producto producto = detallepedido.getProducto();
            if (producto != null) {
                producto = em.getReference(producto.getClass(), producto.getIdProducto());
                detallepedido.setProducto(producto);
            }
            em.persist(detallepedido);
            em.getTransaction().commit();
        } finally {
            if (em != null) { em.close(); }
        }
    }

    // UPDATE: UPDATE detalle_pedido SET ... WHERE id_detalle = ?
    public void edit(Detallepedido detallepedido) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Pedido pedidoNew = detallepedido.getPedido();
            Producto productoNew = detallepedido.getProducto();
            if (pedidoNew != null) {
                pedidoNew = em.getReference(pedidoNew.getClass(), pedidoNew.getIdPedido());
                detallepedido.setPedido(pedidoNew);
            }
            if (productoNew != null) {
                productoNew = em.getReference(productoNew.getClass(), productoNew.getIdProducto());
                detallepedido.setProducto(productoNew);
            }
            detallepedido = em.merge(detallepedido);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                int id = detallepedido.getIdDetalle();
                if (findDetallepedido(id) == null) {
                    throw new NonexistentEntityException("The detallepedido with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) { em.close(); }
        }
    }

    // DELETE: DELETE FROM detalle_pedido WHERE id_detalle = ?
    public void destroy(int id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Detallepedido detallepedido;
            try {
                detallepedido = em.getReference(Detallepedido.class, id);
                detallepedido.getIdDetalle();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The detallepedido with id " + id + " no longer exists.", enfe);
            }
            em.remove(detallepedido);
            em.getTransaction().commit();
        } finally {
            if (em != null) { em.close(); }
        }
    }

    // READ ALL: SELECT * FROM detalle_pedido
    public List<Detallepedido> findDetallepedidoEntities() {
        return findDetallepedidoEntities(true, -1, -1);
    }

    public List<Detallepedido> findDetallepedidoEntities(int maxResults, int firstResult) {
        return findDetallepedidoEntities(false, maxResults, firstResult);
    }

    private List<Detallepedido> findDetallepedidoEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Detallepedido.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    // READ by ID: SELECT * FROM detalle_pedido WHERE id_detalle = ?
    public Detallepedido findDetallepedido(int id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Detallepedido.class, id);
        } finally {
            em.close();
        }
    }

    // COUNT: SELECT COUNT(*) FROM detalle_pedido
    public int getDetallepedidoCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Detallepedido> rt = cq.from(Detallepedido.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
}
