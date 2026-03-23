package persistencias;

import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import logica.Envio;
import logica.Pedido;
import persistencias.exceptions.NonexistentEntityException;

/**
 * EnvioJpaController — CRUD para la entidad Envio (tabla 'envio').
 * Provee: create, edit, destroy, findEnvioEntities, findEnvio, getEnvioCount.
 * Usado por SvEnvios para crear y actualizar envíos desde el panel admin.
 * Resuelve FK con Pedido (1:1) mediante em.getReference().
 */
public class EnvioJpaController implements Serializable {

    // Constructor con EMF explícito
    public EnvioJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    // Crea un nuevo EntityManager (conexión activa a BD)
    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }
    
    // Constructor sin parámetros: usa EMF del Singleton JpaProvider
    public EnvioJpaController() {
        this(JpaProvider.getEntityManagerFactory());
    }

    // CREATE: INSERT INTO envio (...) VALUES (...)
    // Resuelve FK con Pedido mediante getReference()
    public void create(Envio envio) throws Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Pedido pedido = envio.getPedido();
            if (pedido != null) {
                pedido = em.getReference(pedido.getClass(), pedido.getIdPedido());
                envio.setPedido(pedido);
            }
            em.persist(envio);
            em.getTransaction().commit();
        } finally {
            if (em != null) { em.close(); }
        }
    }

    // UPDATE: UPDATE envio SET ... WHERE id_envio = ?
    public void edit(Envio envio) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Pedido pedidoNew = envio.getPedido();
            if (pedidoNew != null) {
                pedidoNew = em.getReference(pedidoNew.getClass(), pedidoNew.getIdPedido());
                envio.setPedido(pedidoNew);
            }
            envio = em.merge(envio);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                int id = envio.getIdEnvio();
                if (findEnvio(id) == null) {
                    throw new NonexistentEntityException("The envio with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) { em.close(); }
        }
    }

    // DELETE: DELETE FROM envio WHERE id_envio = ?
    public void destroy(int id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Envio envio;
            try {
                envio = em.getReference(Envio.class, id);
                envio.getIdEnvio();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The envio with id " + id + " no longer exists.", enfe);
            }
            em.remove(envio);
            em.getTransaction().commit();
        } finally {
            if (em != null) { em.close(); }
        }
    }

    // READ ALL: SELECT * FROM envio
    public List<Envio> findEnvioEntities() {
        return findEnvioEntities(true, -1, -1);
    }

    public List<Envio> findEnvioEntities(int maxResults, int firstResult) {
        return findEnvioEntities(false, maxResults, firstResult);
    }

    private List<Envio> findEnvioEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Envio.class));
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

    // READ by ID: SELECT * FROM envio WHERE id_envio = ?
    public Envio findEnvio(int id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Envio.class, id);
        } finally {
            em.close();
        }
    }

    // COUNT: SELECT COUNT(*) FROM envio
    public int getEnvioCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Envio> rt = cq.from(Envio.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
}
