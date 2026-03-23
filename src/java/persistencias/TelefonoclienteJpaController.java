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

/**
 * TelefonoclienteJpaController — CRUD para la entidad Telefonocliente (tabla 'telefono_cliente').
 * Provee: create, edit, destroy, findTelefonoclienteEntities, findTelefonocliente, getTelefonoclienteCount.
 * Usado por SvContactoCliente para gestionar teléfonos del cliente desde perfil.jsp.
 * Resuelve FK con Cliente mediante em.getReference().
 */
public class TelefonoclienteJpaController implements Serializable {

    // Constructor con EMF explícito
    public TelefonoclienteJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    // Crea un nuevo EntityManager (conexión activa a BD)
    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }
    
    // Constructor sin parámetros: usa EMF del Singleton JpaProvider
    public TelefonoclienteJpaController() {
        this(JpaProvider.getEntityManagerFactory());
    }

    // CREATE: INSERT INTO telefono_cliente (...) VALUES (...)
    // Resuelve FK con Cliente mediante getReference()
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

    // UPDATE: UPDATE telefono_cliente SET ... WHERE id_telefono = ?
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

    // DELETE: DELETE FROM telefono_cliente WHERE id_telefono = ?
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

    // READ ALL: SELECT * FROM telefono_cliente
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

    // READ by ID: SELECT * FROM telefono_cliente WHERE id_telefono = ?
    public Telefonocliente findTelefonocliente(int id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Telefonocliente.class, id);
        } finally {
            em.close();
        }
    }

    // COUNT: SELECT COUNT(*) FROM telefono_cliente
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
