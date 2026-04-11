package persistencias;                                          // declara el paquete al que pertenece esta clase

import java.io.Serializable;                                    // permite serializar la clase (requerido por JPA controllers)
import java.sql.Timestamp;                                      // convierte LocalDateTime a tipo compatible con JDBC/MySQL
import java.util.List;                                          // tipo de retorno para consultas que devuelven múltiples filas
import javax.persistence.EntityManager;                         // interfaz principal de JPA para ejecutar operaciones sobre la BD
import javax.persistence.EntityManagerFactory;                  // fábrica de EntityManagers, una por aplicación
import javax.persistence.Query;                                 // representa una consulta JPA (JPQL o nativa)
import logica.Pedido;                                           // entidad JPA que mapea la tabla 'pedido'
import persistencias.exceptions.NonexistentEntityException;     // excepción propia cuando el registro no existe en BD

/**
 * PedidoJpaController — CRUD para la entidad Pedido (tabla 'pedido').
 * Provee: create, edit, destroy, findPedidoEntities, findPedido, getPedidoCount.
 * Usado por SvCompra (crear pedido), SvPedidos (cambiar estado) y SvPagos/SvEnvios.
 * Toda la persistencia se realiza mediante consultas SQL nativas (createNativeQuery).
 * IMPORTANTE: EclipseLink NO soporta parámetros nombrados (:nombre) en native queries;
 *             se usan parámetros posicionales (?) con setParameter(posición, valor).
 */
public class PedidoJpaController implements Serializable {      // implementa Serializable por convención de JPA controllers

    public PedidoJpaController(EntityManagerFactory emf) {      // constructor que recibe una EMF externa (inyección manual o tests)
        this.emf = emf;                                         // asigna la fábrica recibida al campo de instancia
    }
    private EntityManagerFactory emf = null;                    // fábrica de EntityManagers; se inicializa en el constructor

    public EntityManager getEntityManager() {                   // método auxiliar: crea y retorna un nuevo EntityManager (conexión activa a BD)
        return emf.createEntityManager();                       // cada llamada abre una nueva conexión lógica a la base de datos
    }

    public PedidoJpaController() {                              // constructor sin parámetros: obtiene la EMF del Singleton compartido
        this(JpaProvider.getEntityManagerFactory());            // llama al otro constructor reutilizando la EMF global de la aplicación
    }

    // ─────────────────────────── CREATE ───────────────────────────
    // Inserta un nuevo pedido en la tabla 'pedido' usando SQL nativo.
    // Usa ? posicionales porque EclipseLink no soporta :nombre en native queries.
    // Después del INSERT recupera el ID autogenerado con LAST_INSERT_ID() y lo asigna al objeto.
    public void create(Pedido pedido) throws Exception {         // recibe la entidad Pedido ya construida; lanza Exception si falla la BD
        EntityManager em = null;                                // declarado fuera del try para cerrarlo siempre en el finally
        try {
            em = getEntityManager();                            // abre una nueva conexión a la base de datos
            em.getTransaction().begin();                        // inicia la transacción manualmente (modo RESOURCE_LOCAL, sin JTA)

            // Extrae el ID del cliente desde la relación; usa 0 si el cliente es null (caso defensivo)
            int idCliente = pedido.getCliente() != null        // verifica que el pedido tenga un cliente asociado
                    ? pedido.getCliente().getIdCliente()        // extrae el id_cliente (FK) que va a la tabla
                    : 0;                                        // 0 si por algún motivo el cliente es null

            // Consulta nativa de inserción con parámetros posicionales ?
            // Los ? se numeran implícitamente: posición 1 = primer ?, posición 2 = segundo ?, etc.
            Query q = em.createNativeQuery(                     // createNativeQuery ejecuta SQL puro contra MySQL
                "INSERT INTO pedido (id_cliente, fecha_pedido, estado, created_at, updated_at, total, activo) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)");

            q.setParameter(1, idCliente);                       // posición 1 = id_cliente (FK hacia tabla cliente)

            // LocalDateTime no es soportado directamente por todos los drivers JDBC;
            // Timestamp.valueOf lo convierte al tipo java.sql.Timestamp que MySQL sí entiende siempre
            q.setParameter(2, pedido.getFechaPedido() != null   // posición 2 = fecha_pedido
                    ? Timestamp.valueOf(pedido.getFechaPedido()) // convierte LocalDateTime → java.sql.Timestamp
                    : null);                                    // null si no tiene fecha asignada

            q.setParameter(3, pedido.getEstado() != null        // posición 3 = estado (VARCHAR en MySQL)
                    ? pedido.getEstado().name()                 // .name() retorna el texto del enum: "PENDIENTE", "ENVIADO", etc.
                    : null);                                    // null si no tiene estado aún

            q.setParameter(4, pedido.getCreatedAt() != null     // posición 4 = created_at
                    ? Timestamp.valueOf(pedido.getCreatedAt())  // convierte LocalDateTime → Timestamp
                    : null);

            q.setParameter(5, pedido.getUpdatedAt() != null     // posición 5 = updated_at
                    ? Timestamp.valueOf(pedido.getUpdatedAt())  // convierte LocalDateTime → Timestamp
                    : null);

            q.setParameter(6, pedido.getTotal());               // posición 6 = total (BigDecimal → DECIMAL(10,2) en MySQL)
            q.setParameter(7, pedido.isActivo());               // posición 7 = activo (boolean → TINYINT(1) en MySQL)

            q.executeUpdate();                                  // ejecuta el INSERT; retorna número de filas afectadas (1 si fue exitoso)

            // Recupera el ID que MySQL asignó automáticamente (AUTO_INCREMENT) al nuevo registro
            // LAST_INSERT_ID() es función de MySQL: retorna el último ID generado en esta misma conexión
            Number generatedId = (Number) em.createNativeQuery("SELECT LAST_INSERT_ID()").getSingleResult();
            pedido.setIdPedido(generatedId.intValue());         // asigna el ID al objeto para que el llamador lo conozca

            em.getTransaction().commit();                       // confirma la transacción; el registro queda guardado en MySQL

        } finally {
            if (em != null) { em.close(); }                     // siempre cierra el EntityManager para liberar la conexión
        }
    }

    // ─────────────────────────── UPDATE ───────────────────────────
    // Actualiza todos los campos de un pedido existente usando SQL nativo con ? posicionales.
    // Lanza NonexistentEntityException si executeUpdate() retorna 0 (el id no existe).
    public void edit(Pedido pedido) throws NonexistentEntityException, Exception {
        EntityManager em = null;                                // declarado fuera del try para cerrarlo siempre en el finally
        try {
            em = getEntityManager();                            // abre conexión a la base de datos
            em.getTransaction().begin();                        // inicia transacción manual

            int idCliente = pedido.getCliente() != null        // extrae la FK del cliente de la relación
                    ? pedido.getCliente().getIdCliente()
                    : 0;                                        // 0 como fallback defensivo

            // Consulta nativa UPDATE con ? posicionales; el último ? es el WHERE id_pedido = ?
            Query q = em.createNativeQuery(
                "UPDATE pedido SET id_cliente=?, fecha_pedido=?, estado=?, " +
                "created_at=?, updated_at=?, total=?, activo=? " +
                "WHERE id_pedido=?");

            q.setParameter(1, idCliente);                       // posición 1 = id_cliente (FK del cliente propietario)
            q.setParameter(2, pedido.getFechaPedido() != null   // posición 2 = fecha_pedido
                    ? Timestamp.valueOf(pedido.getFechaPedido())
                    : null);
            q.setParameter(3, pedido.getEstado() != null        // posición 3 = estado como String del enum
                    ? pedido.getEstado().name()
                    : null);
            q.setParameter(4, pedido.getCreatedAt() != null     // posición 4 = created_at
                    ? Timestamp.valueOf(pedido.getCreatedAt())
                    : null);
            q.setParameter(5, pedido.getUpdatedAt() != null     // posición 5 = updated_at
                    ? Timestamp.valueOf(pedido.getUpdatedAt())
                    : null);
            q.setParameter(6, pedido.getTotal());               // posición 6 = total (BigDecimal)
            q.setParameter(7, pedido.isActivo());               // posición 7 = activo (boolean)
            q.setParameter(8, pedido.getIdPedido());            // posición 8 = WHERE id_pedido = este valor (PK del registro a actualizar)

            int updated = q.executeUpdate();                    // ejecuta el UPDATE; retorna cuántas filas fueron modificadas
            em.getTransaction().commit();                       // confirma los cambios en la base de datos

            if (updated == 0) {                                 // si no se modificó ninguna fila, el id no existe en la tabla
                throw new NonexistentEntityException(
                        "The pedido with id " + pedido.getIdPedido() + " no longer exists.");
            }
        } finally {
            if (em != null) { em.close(); }                     // libera la conexión siempre, haya o no excepción
        }
    }

    // ─────────────────────────── DELETE ───────────────────────────
    // Elimina físicamente un pedido de la tabla por su id usando SQL nativo.
    // Lanza NonexistentEntityException si no se eliminó ninguna fila.
    public void destroy(int id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();                            // abre conexión a la base de datos
            em.getTransaction().begin();                        // inicia transacción manual

            // DELETE con ? posicional: el ? corresponde al id_pedido a eliminar
            Query q = em.createNativeQuery("DELETE FROM pedido WHERE id_pedido = ?");
            q.setParameter(1, id);                              // posición 1 = id del pedido a borrar

            int deleted = q.executeUpdate();                    // ejecuta el DELETE; retorna 1 si borró, 0 si no existía
            em.getTransaction().commit();                       // confirma el borrado

            if (deleted == 0) {                                 // 0 filas borradas = el id no existía
                throw new NonexistentEntityException(
                        "The pedido with id " + id + " no longer exists.");
            }
        } finally {
            if (em != null) { em.close(); }                     // siempre cierra la conexión
        }
    }

    // ─────────────────────── READ ALL (sobrecarga) ────────────────
    // Estos dos métodos públicos son la interfaz limpia que ven los servlets.
    // Ambos delegan al método privado de abajo para no duplicar la lógica
    // del EntityManager, el createNativeQuery y el finally em.close().

    // Versión sin parámetros: trae TODOS los pedidos de la tabla sin límite.
    // Se usa cuando el servlet necesita la lista completa, por ejemplo en SvPedidos.doGet().
    // Llama al privado con all=true para que omita el LIMIT/OFFSET en el SQL.
    // Los valores -1,-1 se pasan pero son ignorados porque all=true los descarta.
    public List<Pedido> findPedidoEntities() {
        return findPedidoEntities(true, -1, -1);                // all=true → SELECT * FROM pedido (sin LIMIT ni OFFSET)
    }

    // Versión paginada: trae solo un subconjunto de pedidos.
    // maxResults = cuántos registros devolver (equivale a LIMIT en SQL).
    // firstResult = desde qué posición empezar, contando desde 0 (equivale a OFFSET en SQL).
    // Ejemplo: findPedidoEntities(10, 0)  → primeros 10 pedidos
    //          findPedidoEntities(10, 20) → pedidos del 21 al 30
    // Llama al privado con all=false para que sí agregue el LIMIT/OFFSET al SQL.
    public List<Pedido> findPedidoEntities(int maxResults, int firstResult) {
        return findPedidoEntities(false, maxResults, firstResult); // all=false → SELECT * FROM pedido LIMIT ? OFFSET ?
    }

    // ─────────────────────── READ ALL (implementación) ────────────
    // SELECT * con mapeo automático a Pedido.class via EclipseLink.
    // No hay parámetros externos, así que no hay riesgo con named vs positional.
    private List<Pedido> findPedidoEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();                  // abre conexión; solo lectura, sin transacción
        try {
            String sql = "SELECT * FROM pedido";                // consulta base: todas las columnas de la tabla pedido

            if (!all) {                                         // paginación solicitada: agrega LIMIT y OFFSET
                sql += " LIMIT "  + maxResults                  // LIMIT: cuántas filas devuelve como máximo
                     + " OFFSET " + firstResult;               // OFFSET: desde qué fila empezar (0-based)
            }

            // El segundo argumento Pedido.class le dice a EclipseLink que mapee cada fila SQL
            // a un objeto Pedido usando las anotaciones @Column y @JoinColumn de la entidad
            Query q = em.createNativeQuery(sql, Pedido.class);
            return q.getResultList();                           // ejecuta el SELECT y retorna la lista de entidades mapeadas
        } finally {
            em.close();                                         // libera la conexión aunque ocurra excepción en el mapeo
        }
    }

    // ─────────────────────── READ BY ID ───────────────────────────
    // SELECT * WHERE id_pedido = ? con parámetro posicional (EclipseLink exige ? en native queries).
    // Retorna null si no existe el registro.
    public Pedido findPedido(int id) {
        EntityManager em = getEntityManager();                  // abre conexión de solo lectura
        try {
            // ? posicional obligatorio en EclipseLink para native queries; :id causaría SQLSyntaxErrorException
            Query q = em.createNativeQuery(
                "SELECT * FROM pedido WHERE id_pedido = ?", Pedido.class); // Pedido.class = mapeo automático de fila a entidad
            q.setParameter(1, id);                              // posición 1 = el id_pedido a buscar

            // getResultList() es más seguro que getSingleResult():
            // no lanza NoResultException si el registro no existe, simplemente retorna lista vacía
            List<Pedido> results = q.getResultList();
            return results.isEmpty() ? null : results.get(0);  // null si no existe, o el único Pedido encontrado
        } finally {
            em.close();                                         // libera la conexión siempre
        }
    }

    // ─────────────────────── COUNT ────────────────────────────────
    // COUNT(*) sin parámetros: no hay riesgo de compatibilidad named vs positional.
    public int getPedidoCount() {
        EntityManager em = getEntityManager();                  // abre conexión de solo lectura
        try {
            Query q = em.createNativeQuery("SELECT COUNT(*) FROM pedido"); // cuenta todas las filas de la tabla
            // MySQL retorna COUNT(*) como Long o BigInteger según versión del driver;
            // Number es la superclase común de ambos y evita ClassCastException
            return ((Number) q.getSingleResult()).intValue();   // convierte el escalar a int
        } finally {
            em.close();                                         // libera la conexión
        }
    }
}
