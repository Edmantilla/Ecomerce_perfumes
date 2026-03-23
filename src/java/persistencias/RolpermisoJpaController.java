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

/**
 * RolpermisoJpaController — CRUD para la entidad Rolpermiso (tabla 'rol_permiso').
 * Provee: create, edit, destroy, findRolpermisoEntities, findRolpermiso, getRolpermisoCount.
 * Usado por SvPermisos para asignar/revocar permisos a roles desde el panel admin.
 * Resuelve FK con Rol y Permiso mediante em.getReference().
 */
public class RolpermisoJpaController implements Serializable {

    // Constructor con EMF explícito
    public RolpermisoJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    // Crea un nuevo EntityManager (conexión activa a BD)
    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }
    
    // Constructor sin parámetros: usa EMF del Singleton JpaProvider
    public RolpermisoJpaController() {
        this(JpaProvider.getEntityManagerFactory());
    }

    // CREATE: INSERT INTO rol_permiso (...) VALUES (...)
    // Resuelve FK con Rol y Permiso mediante getReference()
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

    // UPDATE: UPDATE rol_permiso SET ... WHERE id_rol_permiso = ?
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

    // DELETE: DELETE FROM rol_permiso WHERE id_rol_permiso = ?
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

    // READ ALL: SELECT * FROM rol_permiso
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

    // READ by ID: SELECT * FROM rol_permiso WHERE id_rol_permiso = ?
    public Rolpermiso findRolpermiso(int id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Rolpermiso.class, id);
        } finally {
            em.close();
        }
    }

    // COUNT: SELECT COUNT(*) FROM rol_permiso
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
