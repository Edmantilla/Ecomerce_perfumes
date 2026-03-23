package persistencias;

import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import logica.Permiso;
import persistencias.exceptions.NonexistentEntityException;

/**
 * PermisoJpaController — CRUD para la entidad Permiso (tabla 'permiso').
 * Provee: create, edit, destroy, findPermisoEntities, findPermiso, getPermisoCount.
 * Usado por SvPermisos para gestionar permisos desde el panel admin.
 * Sin FK complejas (Permiso no depende de otras tablas).
 */
public class PermisoJpaController implements Serializable {

    // Constructor con EMF explícito
    public PermisoJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    // Crea un nuevo EntityManager (conexión activa a BD)
    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }
    
    // Constructor sin parámetros: usa EMF del Singleton JpaProvider
    public PermisoJpaController() {
        this(JpaProvider.getEntityManagerFactory());
    }

    // CREATE: INSERT INTO permiso (...) VALUES (...)
    public void create(Permiso permiso) throws Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(permiso);
            em.getTransaction().commit();
        } finally {
            if (em != null) { em.close(); }
        }
    }

    // UPDATE: UPDATE permiso SET ... WHERE id_permiso = ?
    public void edit(Permiso permiso) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            permiso = em.merge(permiso);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                int id = permiso.getIdPermiso();
                if (findPermiso(id) == null) {
                    throw new NonexistentEntityException("The permiso with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) { em.close(); }
        }
    }

    // DELETE: DELETE FROM permiso WHERE id_permiso = ?
    public void destroy(int id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Permiso permiso;
            try {
                permiso = em.getReference(Permiso.class, id);
                permiso.getIdPermiso();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The permiso with id " + id + " no longer exists.", enfe);
            }
            em.remove(permiso);
            em.getTransaction().commit();
        } finally {
            if (em != null) { em.close(); }
        }
    }

    // READ ALL: SELECT * FROM permiso
    public List<Permiso> findPermisoEntities() {
        return findPermisoEntities(true, -1, -1);
    }

    public List<Permiso> findPermisoEntities(int maxResults, int firstResult) {
        return findPermisoEntities(false, maxResults, firstResult);
    }

    private List<Permiso> findPermisoEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Permiso.class));
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

    // READ by ID: SELECT * FROM permiso WHERE id_permiso = ?
    public Permiso findPermiso(int id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Permiso.class, id);
        } finally {
            em.close();
        }
    }

    // COUNT: SELECT COUNT(*) FROM permiso
    public int getPermisoCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Permiso> rt = cq.from(Permiso.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
}
