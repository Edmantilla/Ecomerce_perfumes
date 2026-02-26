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

public class CorreoclienteJpaController implements Serializable {

    public CorreoclienteJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }
    
    public CorreoclienteJpaController() {
        this(JpaProvider.getEntityManagerFactory());
    }
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

    public Correocliente findCorreocliente(int id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Correocliente.class, id);
        } finally {
            em.close();
        }
    }

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
