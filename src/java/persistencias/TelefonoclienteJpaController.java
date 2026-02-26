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
import logica.Telefonocliente;
import persistencias.exceptions.NonexistentEntityException;

public class TelefonoclienteJpaController implements Serializable {

    public TelefonoclienteJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }
    
    public TelefonoclienteJpaController() {
        this(JpaProvider.getEntityManagerFactory());
    }
    
    public void create(Telefonocliente telefonocliente) throws Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Cliente cliente = telefonocliente.getCliente();
            if (cliente != null) {
                cliente = em.getReference(cliente.getClass(), cliente.getIdCliente());
                telefonocliente.setCliente(cliente);
            }
            em.persist(telefonocliente);
            em.getTransaction().commit();
        } finally {
            if (em != null) { em.close(); }
        }
    }

    public void edit(Telefonocliente telefonocliente) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Cliente clienteNew = telefonocliente.getCliente();
            if (clienteNew != null) {
                clienteNew = em.getReference(clienteNew.getClass(), clienteNew.getIdCliente());
                telefonocliente.setCliente(clienteNew);
            }
            telefonocliente = em.merge(telefonocliente);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                int id = telefonocliente.getIdTelefono();
                if (findTelefonocliente(id) == null) {
                    throw new NonexistentEntityException("The telefonocliente with id " + id + " no longer exists.");
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
            Telefonocliente telefonocliente;
            try {
                telefonocliente = em.getReference(Telefonocliente.class, id);
                telefonocliente.getIdTelefono();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The telefonocliente with id " + id + " no longer exists.", enfe);
            }
            em.remove(telefonocliente);
            em.getTransaction().commit();
        } finally {
            if (em != null) { em.close(); }
        }
    }

    public List<Telefonocliente> findTelefonoclienteEntities() {
        return findTelefonoclienteEntities(true, -1, -1);
    }

    public List<Telefonocliente> findTelefonoclienteEntities(int maxResults, int firstResult) {
        return findTelefonoclienteEntities(false, maxResults, firstResult);
    }

    private List<Telefonocliente> findTelefonoclienteEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Telefonocliente.class));
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

    public Telefonocliente findTelefonocliente(int id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Telefonocliente.class, id);
        } finally {
            em.close();
        }
    }

    public int getTelefonoclienteCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Telefonocliente> rt = cq.from(Telefonocliente.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
}
