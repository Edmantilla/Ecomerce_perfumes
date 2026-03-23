package persistencias;

import java.io.Serializable;                      // permite serializar el controlador
import java.util.List;                             // lista de resultados
import javax.persistence.EntityManager;            // gestor de entidades JPA (conexión activa a BD)
import javax.persistence.EntityManagerFactory;     // fábrica que crea EntityManagers
import javax.persistence.EntityNotFoundException;  // excepción si la entidad no existe
import javax.persistence.Query;                    // consulta JPA
import javax.persistence.criteria.CriteriaQuery;  // API de consultas tipo-seguro
import javax.persistence.criteria.Root;            // raíz de la consulta
import logica.Cliente;                             // entidad Cliente (relación 1:1)
import logica.Rol;                                 // entidad Rol (relación N:1)
import logica.Usuario;                             // entidad Usuario que este controller gestiona
import persistencias.exceptions.NonexistentEntityException; // excepción si el registro no existe

/**
 * UsuarioJpaController — Controlador de persistencia para la entidad Usuario.
 * Provee operaciones CRUD contra la tabla 'usuario'.
 * Resuelve las FK con Cliente (1:1, opcional) y Rol (N:1, obligatorio)
 * usando em.getReference() para evitar cargas innecesarias.
 * Usado por SvLogin, SvRegistro y SvPermisos.
 */
public class UsuarioJpaController implements Serializable {

    // Constructor que recibe la fábrica de EntityManagers
    public UsuarioJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null; // referencia a la fábrica de conexiones

    // Crea un nuevo EntityManager (conexión activa a BD)
    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }
    
    // Constructor sin parámetros: usa el EMF del Singleton JpaProvider
    public UsuarioJpaController() {
        this(JpaProvider.getEntityManagerFactory());
    }
    // CREATE: inserta un nuevo usuario en la tabla 'usuario'
    // Resuelve FK de Cliente (opcional) y Rol (obligatorio) con getReference()
    public void create(Usuario usuario) throws Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();             // abre conexión
            em.getTransaction().begin();         // inicia transacción
            Cliente cliente = usuario.getCliente();
            if (cliente != null) {               // cliente es opcional (null = admin puro)
                cliente = em.getReference(cliente.getClass(), cliente.getIdCliente());
                usuario.setCliente(cliente);      // vincula proxy del cliente
            }
            Rol rol = usuario.getRol();
            if (rol != null) {                   // rol es obligatorio
                rol = em.getReference(rol.getClass(), rol.getIdRol());
                usuario.setRol(rol);             // vincula proxy del rol
            }
            em.persist(usuario);                 // INSERT INTO usuario (...) VALUES (...)
            em.getTransaction().commit();         // confirma transacción
        } finally {
            if (em != null) { em.close(); }      // cierra conexión siempre
        }
    }

    // UPDATE: actualiza un usuario existente en la tabla 'usuario'
    // Usado por SvLogin (registrar último acceso), SvPermisos (cambiar rol), etc.
    public void edit(Usuario usuario) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();             // abre conexión
            em.getTransaction().begin();         // inicia transacción
            Cliente clienteNew = usuario.getCliente();
            Rol rolNew = usuario.getRol();
            if (clienteNew != null) {
                clienteNew = em.getReference(clienteNew.getClass(), clienteNew.getIdCliente());
                usuario.setCliente(clienteNew);  // resuelve FK cliente
            }
            if (rolNew != null) {
                rolNew = em.getReference(rolNew.getClass(), rolNew.getIdRol());
                usuario.setRol(rolNew);          // resuelve FK rol
            }
            usuario = em.merge(usuario);          // UPDATE usuario SET ... WHERE id_usuario = ?
            em.getTransaction().commit();         // confirma cambios
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                int id = usuario.getIdUsuario();
                if (findUsuario(id) == null) {   // verifica si el usuario aún existe
                    throw new NonexistentEntityException("The usuario with id " + id + " no longer exists.");
                }
            }
            throw ex;                            // relanza excepción
        } finally {
            if (em != null) { em.close(); }      // cierra conexión siempre
        }
    }

    // DELETE: elimina físicamente un usuario de la tabla 'usuario'
    public void destroy(int id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();             // abre conexión
            em.getTransaction().begin();         // inicia transacción
            Usuario usuario;
            try {
                usuario = em.getReference(Usuario.class, id); // proxy
                usuario.getIdUsuario();          // fuerza carga (dispara excepción si no existe)
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The usuario with id " + id + " no longer exists.", enfe);
            }
            em.remove(usuario);                  // DELETE FROM usuario WHERE id_usuario = ?
            em.getTransaction().commit();         // confirma eliminación
        } finally {
            if (em != null) { em.close(); }      // cierra conexión
        }
    }

    // READ ALL: retorna TODOS los usuarios (SELECT * FROM usuario)
    public List<Usuario> findUsuarioEntities() {
        return findUsuarioEntities(true, -1, -1);
    }

    // READ con paginación: retorna subconjunto de usuarios (LIMIT/OFFSET)
    public List<Usuario> findUsuarioEntities(int maxResults, int firstResult) {
        return findUsuarioEntities(false, maxResults, firstResult);
    }

    // Método interno: construye la consulta con Criteria API
    private List<Usuario> findUsuarioEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Usuario.class));   // SELECT * FROM usuario
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);      // LIMIT
                q.setFirstResult(firstResult);    // OFFSET
            }
            return q.getResultList();             // ejecuta y retorna lista
        } finally {
            em.close();                           // cierra conexión
        }
    }

    // READ by ID: busca un usuario por su clave primaria
    // Retorna null si no existe
    public Usuario findUsuario(int id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Usuario.class, id);    // SELECT * FROM usuario WHERE id_usuario = ?
        } finally {
            em.close();
        }
    }

    // COUNT: retorna el número total de usuarios
    // SELECT COUNT(*) FROM usuario
    public int getUsuarioCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Usuario> rt = cq.from(Usuario.class);
            cq.select(em.getCriteriaBuilder().count(rt)); // COUNT(*)
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
}
