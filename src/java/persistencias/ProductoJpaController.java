package persistencias;

import java.io.Serializable;                      // permite serializar el controlador (requerido por JPA)
import java.util.List;                             // lista de resultados
import javax.persistence.EntityManager;            // gestor de entidades JPA (conexión activa a BD)
import javax.persistence.EntityManagerFactory;     // fábrica que crea EntityManagers
import javax.persistence.EntityNotFoundException;  // excepción cuando no se encuentra la entidad por ID
import javax.persistence.Query;                    // consulta JPA
import javax.persistence.criteria.CriteriaQuery;  // API de consultas tipo-seguro (Criteria API)
import javax.persistence.criteria.Root;            // raíz de la consulta (tabla principal)
import logica.Categoria;                           // entidad Categoria (relación N:1)
import logica.Marca;                               // entidad Marca (relación N:1)
import logica.Producto;                            // entidad Producto que este controller gestiona
import persistencias.exceptions.NonexistentEntityException; // excepción si el registro no existe

/**
 * ProductoJpaController — Controlador de persistencia para la entidad Producto.
 * Provee operaciones CRUD (Create, Read, Update, Delete) contra la tabla 'producto'.
 * Cada método abre su propio EntityManager, ejecuta la operación y lo cierra.
 * Las relaciones con Categoria y Marca se resuelven con em.getReference() para
 * evitar cargar el objeto completo al hacer INSERT o UPDATE.
 * Generado parcialmente por NetBeans y extendido manualmente.
 */
public class ProductoJpaController implements Serializable {

    // Constructor que recibe la fábrica de EntityManagers (inyección manual)
    public ProductoJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null; // referencia a la fábrica de conexiones

    // Crea y retorna un nuevo EntityManager (conexión activa a BD)
    // Cada operación CRUD crea su propio EM y lo cierra al finalizar
    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }
    
    // Constructor sin parámetros: usa el EMF del Singleton JpaProvider
    // Permite crear el controller sin pasar la fábrica explícitamente
    public ProductoJpaController() {
        this(JpaProvider.getEntityManagerFactory());
    }

    // CREATE: inserta un nuevo producto en la tabla 'producto'
    // Resuelve las FK de Categoria y Marca con getReference() (proxy sin cargar toda la entidad)
    // em.persist() hace el INSERT en MySQL
    public void create(Producto producto) throws Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();             // abre conexión
            em.getTransaction().begin();         // inicia transacción
            Categoria categoria = producto.getCategoria();
            if (categoria != null) {
                // getReference() obtiene un proxy de la categoría sin hacer SELECT completo
                categoria = em.getReference(categoria.getClass(), categoria.getIdCategoria());
                producto.setCategoria(categoria); // vincula el proxy al producto
            }
            Marca marca = producto.getMarca();
            if (marca != null) {
                marca = em.getReference(marca.getClass(), marca.getIdMarca());
                producto.setMarca(marca);        // vincula el proxy al producto
            }
            em.persist(producto);                 // INSERT INTO producto (...) VALUES (...)
            em.getTransaction().commit();         // confirma la transacción
        } finally {
            if (em != null) { em.close(); }      // cierra la conexión siempre
        }
    }

    // UPDATE: actualiza un producto existente en la tabla 'producto'
    // em.merge() sincroniza el estado del objeto Java con la BD (UPDATE SET ...)
    // Si el producto ya no existe, lanza NonexistentEntityException
    public void edit(Producto producto) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();             // abre conexión
            em.getTransaction().begin();         // inicia transacción
            Categoria categoriaNew = producto.getCategoria();
            Marca marcaNew = producto.getMarca();
            if (categoriaNew != null) {
                categoriaNew = em.getReference(categoriaNew.getClass(), categoriaNew.getIdCategoria());
                producto.setCategoria(categoriaNew); // resuelve FK
            }
            if (marcaNew != null) {
                marcaNew = em.getReference(marcaNew.getClass(), marcaNew.getIdMarca());
                producto.setMarca(marcaNew);     // resuelve FK
            }
            producto = em.merge(producto);        // UPDATE producto SET ... WHERE id_producto = ?
            em.getTransaction().commit();         // confirma cambios
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                int id = producto.getIdProducto();
                if (findProducto(id) == null) {  // verifica si el producto aún existe
                    throw new NonexistentEntityException("The producto with id " + id + " no longer exists.");
                }
            }
            throw ex;                            // relanza la excepción original
        } finally {
            if (em != null) { em.close(); }      // cierra conexión siempre
        }
    }

    // DELETE: elimina físicamente un producto de la tabla 'producto'
    // Usa getReference() para obtener un proxy y luego em.remove() para el DELETE
    // Si el producto no existe, lanza NonexistentEntityException
    public void destroy(int id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();             // abre conexión
            em.getTransaction().begin();         // inicia transacción
            Producto producto;
            try {
                producto = em.getReference(Producto.class, id); // proxy del producto
                producto.getIdProducto();        // fuerza la carga del proxy (dispara EntityNotFoundException si no existe)
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The producto with id " + id + " no longer exists.", enfe);
            }
            em.remove(producto);                 // DELETE FROM producto WHERE id_producto = ?
            em.getTransaction().commit();         // confirma eliminación
        } finally {
            if (em != null) { em.close(); }      // cierra conexión siempre
        }
    }

    // READ ALL: retorna TODOS los productos (SELECT * FROM producto)
    public List<Producto> findProductoEntities() {
        return findProductoEntities(true, -1, -1);
    }

    // READ con paginación: retorna un subconjunto de productos (LIMIT/OFFSET)
    public List<Producto> findProductoEntities(int maxResults, int firstResult) {
        return findProductoEntities(false, maxResults, firstResult);
    }

    // Método interno: construye la consulta con Criteria API
    // Si all=true retorna todos; si all=false aplica paginación
    private List<Producto> findProductoEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery(); // crea query builder
            cq.select(cq.from(Producto.class));                      // SELECT * FROM producto
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);    // LIMIT
                q.setFirstResult(firstResult);  // OFFSET
            }
            return q.getResultList();           // ejecuta y retorna la lista
        } finally {
            em.close();                         // cierra conexión
        }
    }

    // READ by ID: busca un producto por su clave primaria
    // Retorna null si no existe (em.find no lanza excepción)
    public Producto findProducto(int id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Producto.class, id); // SELECT * FROM producto WHERE id_producto = ?
        } finally {
            em.close();
        }
    }

    // COUNT: retorna el número total de productos en la tabla
    // SELECT COUNT(*) FROM producto
    public int getProductoCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Producto> rt = cq.from(Producto.class);             // raíz: tabla producto
            cq.select(em.getCriteriaBuilder().count(rt));            // COUNT(*)
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();           // convierte Long a int
        } finally {
            em.close();
        }
    }
}
