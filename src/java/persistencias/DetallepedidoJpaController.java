package persistencias;                                          // declara el paquete al que pertenece esta clase

import java.io.Serializable;                                    // permite serializar la clase (convención de JPA controllers)
import java.util.List;                                          // tipo de retorno para consultas que devuelven múltiples filas
import javax.persistence.EntityManager;                         // interfaz principal de JPA para ejecutar operaciones sobre la BD
import javax.persistence.EntityManagerFactory;                  // fábrica de EntityManagers, una por aplicación
import javax.persistence.Query;                                 // representa una consulta JPA (JPQL o nativa SQL)
import logica.Detallepedido;                                    // entidad JPA que mapea la tabla 'detalle_pedido'
import persistencias.exceptions.NonexistentEntityException;     // excepción propia cuando el registro no existe en BD

/**
 * DetallepedidoJpaController — CRUD para la entidad Detallepedido (tabla 'detalle_pedido').
 * Provee: create, edit, destroy, findDetallepedidoEntities, findDetallepedido, getDetallepedidoCount.
 * Usado por SvCompra para crear las líneas de detalle al hacer checkout.
 * Toda la persistencia se realiza mediante consultas SQL nativas (createNativeQuery).
 * IMPORTANTE: EclipseLink NO soporta parámetros nombrados (:nombre) en native queries;
 *             se usan parámetros posicionales (?) con setParameter(posición, valor).
 */
public class DetallepedidoJpaController implements Serializable { // implementa Serializable por convención de JPA controllers

    public DetallepedidoJpaController(EntityManagerFactory emf) { // constructor que recibe una EMF externa
        this.emf = emf;                                           // asigna la fábrica recibida al campo de instancia
    }
    private EntityManagerFactory emf = null;                    // fábrica de EntityManagers; se inicializa en el constructor

    public EntityManager getEntityManager() {                   // método auxiliar: crea y retorna un nuevo EntityManager
        return emf.createEntityManager();                       // cada llamada abre una nueva conexión lógica a la base de datos
    }

    public DetallepedidoJpaController() {                       // constructor sin parámetros: usa la EMF del Singleton compartido
        this(JpaProvider.getEntityManagerFactory());            // llama al otro constructor reutilizando la EMF global
    }

    // ─────────────────────────── CREATE ───────────────────────────
    // Inserta un nuevo detalle de pedido usando SQL nativo con ? posicionales.
    // Recupera el ID autogenerado con LAST_INSERT_ID() y lo asigna al objeto.
    public void create(Detallepedido detallepedido) throws Exception {
        EntityManager em = null;                                // declarado fuera del try para cerrarlo siempre en el finally
        try {
            em = getEntityManager();                            // abre una nueva conexión a la base de datos
            em.getTransaction().begin();                        // inicia la transacción manualmente (RESOURCE_LOCAL)

            // Extrae las FKs de las relaciones; usa 0 como fallback defensivo si son null
            int idPedido = detallepedido.getPedido() != null    // verifica que el detalle tenga pedido asociado
                    ? detallepedido.getPedido().getIdPedido()   // extrae id_pedido (FK hacia tabla pedido)
                    : 0;
            int idProducto = detallepedido.getProducto() != null // verifica que el detalle tenga producto asociado
                    ? detallepedido.getProducto().getIdProducto() // extrae id_producto (FK hacia tabla producto)
                    : 0;

            // Consulta nativa de inserción con ? posicionales
            // EclipseLink exige ? en native queries; :nombre causaría SQLSyntaxErrorException
            Query q = em.createNativeQuery(
                "INSERT INTO detalle_pedido (id_pedido, id_producto, cantidad, precio_unitario, activo) " +
                "VALUES (?, ?, ?, ?, ?)");

            q.setParameter(1, idPedido);                        // posición 1 = id_pedido (FK hacia tabla pedido)
            q.setParameter(2, idProducto);                      // posición 2 = id_producto (FK hacia tabla producto)
            q.setParameter(3, detallepedido.getCantidad());     // posición 3 = cantidad (int → INT en MySQL)
            q.setParameter(4, detallepedido.getPrecioUnitario()); // posición 4 = precio_unitario (BigDecimal → DECIMAL(10,2))
            q.setParameter(5, detallepedido.isActivo());        // posición 5 = activo (boolean → TINYINT(1) en MySQL)

            q.executeUpdate();                                  // ejecuta el INSERT; retorna 1 si fue exitoso

            // Recupera el ID autogenerado (AUTO_INCREMENT) asignado por MySQL al nuevo registro
            // LAST_INSERT_ID() retorna el último ID generado en esta misma conexión (thread-safe)
            Number generatedId = (Number) em.createNativeQuery("SELECT LAST_INSERT_ID()").getSingleResult();
            detallepedido.setIdDetalle(generatedId.intValue()); // asigna el ID al objeto para que el llamador lo conozca

            em.getTransaction().commit();                       // confirma la transacción; el registro queda en MySQL

        } finally {
            if (em != null) { em.close(); }                     // siempre cierra el EntityManager para liberar la conexión
        }
    }

    // ─────────────────────────── UPDATE ───────────────────────────
    // Actualiza todos los campos del detalle usando SQL nativo con ? posicionales.
    // Lanza NonexistentEntityException si executeUpdate() retorna 0 (el id no existe).
    public void edit(Detallepedido detallepedido) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();                            // abre conexión a la base de datos
            em.getTransaction().begin();                        // inicia transacción manual

            // Extrae las FKs de las relaciones
            int idPedido = detallepedido.getPedido() != null
                    ? detallepedido.getPedido().getIdPedido()
                    : 0;
            int idProducto = detallepedido.getProducto() != null
                    ? detallepedido.getProducto().getIdProducto()
                    : 0;

            // UPDATE con ? posicionales; el último ? es el WHERE id_detalle = ?
            Query q = em.createNativeQuery(
                "UPDATE detalle_pedido SET id_pedido=?, id_producto=?, " +
                "cantidad=?, precio_unitario=?, activo=? " +
                "WHERE id_detalle=?");

            q.setParameter(1, idPedido);                        // posición 1 = id_pedido (FK del pedido padre)
            q.setParameter(2, idProducto);                      // posición 2 = id_producto (FK del producto)
            q.setParameter(3, detallepedido.getCantidad());     // posición 3 = cantidad nueva
            q.setParameter(4, detallepedido.getPrecioUnitario()); // posición 4 = precio_unitario nuevo (BigDecimal)
            q.setParameter(5, detallepedido.isActivo());        // posición 5 = activo nuevo (boolean)
            q.setParameter(6, detallepedido.getIdDetalle());    // posición 6 = WHERE id_detalle = este valor (PK a actualizar)

            int updated = q.executeUpdate();                    // ejecuta el UPDATE; 0 si el id no existía
            em.getTransaction().commit();                       // confirma los cambios

            if (updated == 0) {                                 // ninguna fila modificada = el detalle no existe
                throw new NonexistentEntityException(
                        "The detallepedido with id " + detallepedido.getIdDetalle() + " no longer exists.");
            }
        } finally {
            if (em != null) { em.close(); }                     // libera la conexión siempre
        }
    }

    // ─────────────────────────── DELETE ───────────────────────────
    // Elimina físicamente un detalle por su id_detalle usando SQL nativo con ? posicional.
    // Lanza NonexistentEntityException si no se eliminó ninguna fila.
    public void destroy(int id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();                            // abre conexión
            em.getTransaction().begin();                        // inicia transacción manual

            // DELETE con ? posicional: identifica la fila exacta a eliminar por su PK
            Query q = em.createNativeQuery("DELETE FROM detalle_pedido WHERE id_detalle = ?");
            q.setParameter(1, id);                              // posición 1 = id_detalle a borrar

            int deleted = q.executeUpdate();                    // ejecuta el DELETE; 0 si el id no existía
            em.getTransaction().commit();                       // confirma el borrado

            if (deleted == 0) {                                 // 0 filas borradas = el detalle no existía
                throw new NonexistentEntityException(
                        "The detallepedido with id " + id + " no longer exists.");
            }
        } finally {
            if (em != null) { em.close(); }                     // siempre cierra la conexión
        }
    }

    // ─────────────────────── READ ALL (sobrecarga) ────────────────
    public List<Detallepedido> findDetallepedidoEntities() {
        return findDetallepedidoEntities(true, -1, -1);         // all=true: sin límite de filas
    }

    public List<Detallepedido> findDetallepedidoEntities(int maxResults, int firstResult) {
        return findDetallepedidoEntities(false, maxResults, firstResult); // all=false: activa LIMIT/OFFSET
    }

    // ─────────────────────── READ ALL (implementación) ────────────
    // SELECT * con mapeo automático a Detallepedido.class via EclipseLink.
    // Sin parámetros externos, no hay conflicto named vs positional.
    private List<Detallepedido> findDetallepedidoEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();                  // abre conexión de solo lectura
        try {
            String sql = "SELECT * FROM detalle_pedido";        // consulta base: todas las columnas

            if (!all) {                                         // paginación: agrega LIMIT y OFFSET al SQL
                sql += " LIMIT "  + maxResults                  // LIMIT: máximo de filas a retornar
                     + " OFFSET " + firstResult;               // OFFSET: desde qué posición empezar (0-based)
            }

            // Detallepedido.class le dice a EclipseLink que mapee cada fila al objeto Detallepedido
            // usando las anotaciones @Column y @JoinColumn de la entidad automáticamente
            Query q = em.createNativeQuery(sql, Detallepedido.class);
            return q.getResultList();                           // ejecuta el SELECT y retorna la lista de entidades
        } finally {
            em.close();                                         // libera la conexión aunque ocurra excepción
        }
    }

    // ─────────────────────── READ BY ID ───────────────────────────
    // SELECT * WHERE id_detalle = ? con parámetro posicional (obligatorio en EclipseLink native queries).
    // Retorna null si no existe el registro.
    public Detallepedido findDetallepedido(int id) {
        EntityManager em = getEntityManager();                  // abre conexión de solo lectura
        try {
            // ? posicional obligatorio; :id causaría el error SQLSyntaxErrorException en EclipseLink
            Query q = em.createNativeQuery(
                "SELECT * FROM detalle_pedido WHERE id_detalle = ?", Detallepedido.class);
            q.setParameter(1, id);                              // posición 1 = id_detalle a buscar

            // getResultList() no lanza excepción si no hay resultados (más seguro que getSingleResult())
            List<Detallepedido> results = q.getResultList();
            return results.isEmpty() ? null : results.get(0);  // null si no existe, o el único detalle encontrado
        } finally {
            em.close();                                         // libera la conexión siempre
        }
    }

    // ─────────────────────── COUNT ────────────────────────────────
    // COUNT(*) sin parámetros: no hay riesgo de compatibilidad.
    public int getDetallepedidoCount() {
        EntityManager em = getEntityManager();                  // abre conexión de solo lectura
        try {
            Query q = em.createNativeQuery("SELECT COUNT(*) FROM detalle_pedido"); // cuenta todas las filas
            // MySQL puede retornar COUNT(*) como Long o BigInteger según la versión del driver JDBC;
            // castear a Number (superclase de ambos) evita ClassCastException
            return ((Number) q.getSingleResult()).intValue();   // convierte el escalar a int
        } finally {
            em.close();                                         // libera la conexión
        }
    }
}
