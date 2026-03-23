package persistencias;

import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import logica.Cliente;
import logica.Correocliente;
import persistencias.exceptions.NonexistentEntityException;

/**
 * CorreoclienteJpaController — CRUD para la entidad Correocliente (tabla 'correo_cliente').
 * Provee: create, edit, destroy, findCorreoclienteEntities, findCorreocliente, getCorreoclienteCount.
 * Usado por SvContactoCliente para gestionar correos adicionales del cliente desde perfil.jsp.
 * Resuelve FK con Cliente mediante em.getReference().
 */
public class CorreoclienteJpaController implements Serializable {

    // Constructor con EMF explícito
    public CorreoclienteJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    // Crea un nuevo EntityManager (conexión activa a BD)
    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }
    
    // Constructor sin parámetros: usa EMF del Singleton JpaProvider
    public CorreoclienteJpaController() {
        this(JpaProvider.getEntityManagerFactory());
    }

    // CREATE: INSERT INTO correo_cliente (...) VALUES (...)
    // Resuelve FK con Cliente mediante getReference()
    public void create(Correocliente correocliente) throws Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Cliente cliente = correocliente.getCliente();
            if (cliente != null) {
                cliente = em.getReference(cliente.getClass(), cliente.getIdCliente());
                correocliente.setCliente(cliente);
            }
            em.persist(correocliente);
            em.getTransaction().commit();
        } finally {
            if (em != null) { em.close(); }
        }
    }

    // UPDATE: UPDATE correo_cliente SET ... WHERE id_correo = ?
    public void edit(Correocliente correocliente) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Cliente clienteNew = correocliente.getCliente();
            if (clienteNew != null) {
                clienteNew = em.getReference(clienteNew.getClass(), clienteNew.getIdCliente());
                correocliente.setCliente(clienteNew);
            }
            correocliente = em.merge(correocliente);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                int id = correocliente.getIdCorreo();
                if (findCorreocliente(id) == null) {
                    throw new NonexistentEntityException("The correocliente with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) { em.close(); }
        }
    }

    // DELETE: DELETE FROM correo_cliente WHERE id_correo = ?
    public void destroy(int id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Correocliente correocliente;
            try {
                correocliente = em.getReference(Correocliente.class, id);
                correocliente.getIdCorreo();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The correocliente with id " + id + " no longer exists.", enfe);
            }
            em.remove(correocliente);
            em.getTransaction().commit();
        } finally {
            if (em != null) { em.close(); }
        }
    }

    // READ ALL: SELECT * FROM correo_cliente
    public List<Correocliente> findCorreoclienteEntities() {
        return findCorreoclienteEntities(true, -1, -1);
    }

    public List<Correocliente> findCorreoclienteEntities(int maxResults, int firstResult) {
        return findCorreoclienteEntities(false, maxResults, firstResult);
    }

    private List<Correocliente> findCorreoclienteEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Correocliente.class));
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

    // READ by ID: SELECT * FROM correo_cliente WHERE id_correo = ?
    public Correocliente findCorreocliente(int id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Correocliente.class, id);
        } finally {
            em.close();
        }
    }

    // COUNT: SELECT COUNT(*) FROM correo_cliente
    public int getCorreoclienteCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Correocliente> rt = cq.from(Correocliente.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
}
