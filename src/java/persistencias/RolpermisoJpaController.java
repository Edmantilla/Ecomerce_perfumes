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
import logica.Rol;
import logica.Rolpermiso;
import persistencias.exceptions.NonexistentEntityException;

public class RolpermisoJpaController implements Serializable {

    public RolpermisoJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }
    
    public RolpermisoJpaController() {
        this(JpaProvider.getEntityManagerFactory());
    }

    public void create(Rolpermiso rolpermiso) throws Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Rol rol = rolpermiso.getRol();
            if (rol != null) {
                rol = em.getReference(rol.getClass(), rol.getIdRol());
                rolpermiso.setRol(rol);
            }
            Permiso permiso = rolpermiso.getPermiso();
            if (permiso != null) {
                permiso = em.getReference(permiso.getClass(), permiso.getIdPermiso());
                rolpermiso.setPermiso(permiso);
            }
            em.persist(rolpermiso);
            em.getTransaction().commit();
        } finally {
            if (em != null) { em.close(); }
        }
    }

    public void edit(Rolpermiso rolpermiso) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Rol rolNew = rolpermiso.getRol();
            Permiso permisoNew = rolpermiso.getPermiso();
            if (rolNew != null) {
                rolNew = em.getReference(rolNew.getClass(), rolNew.getIdRol());
                rolpermiso.setRol(rolNew);
            }
            if (permisoNew != null) {
                permisoNew = em.getReference(permisoNew.getClass(), permisoNew.getIdPermiso());
                rolpermiso.setPermiso(permisoNew);
            }
            rolpermiso = em.merge(rolpermiso);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                int id = rolpermiso.getIdRolPermiso();
                if (findRolpermiso(id) == null) {
                    throw new NonexistentEntityException("The rolpermiso with id " + id + " no longer exists.");
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
            Rolpermiso rolpermiso;
            try {
                rolpermiso = em.getReference(Rolpermiso.class, id);
                rolpermiso.getIdRolPermiso();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The rolpermiso with id " + id + " no longer exists.", enfe);
            }
            em.remove(rolpermiso);
            em.getTransaction().commit();
        } finally {
            if (em != null) { em.close(); }
        }
    }

    public List<Rolpermiso> findRolpermisoEntities() {
        return findRolpermisoEntities(true, -1, -1);
    }

    public List<Rolpermiso> findRolpermisoEntities(int maxResults, int firstResult) {
        return findRolpermisoEntities(false, maxResults, firstResult);
    }

    private List<Rolpermiso> findRolpermisoEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Rolpermiso.class));
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

    public Rolpermiso findRolpermiso(int id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Rolpermiso.class, id);
        } finally {
            em.close();
        }
    }

    public int getRolpermisoCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Rolpermiso> rt = cq.from(Rolpermiso.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
}
