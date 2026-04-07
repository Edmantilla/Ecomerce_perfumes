package persistencias; // declara que esta clase pertenece al paquete "persistencias"

import java.io.Serializable;                     // permite que esta clase pueda convertirse en bytes (requerido por JPA)
import java.sql.Timestamp;                        // tipo de Java que representa fecha+hora; MySQL lo necesita para columnas DATETIME
import java.util.List;                            // permite usar listas (List<Producto>) como tipo de retorno
import javax.persistence.EntityManager;           // clase de JPA que representa la sesión activa con la base de datos
import javax.persistence.EntityManagerFactory;    // fábrica que crea EntityManagers; se crea una sola vez en toda la app
import logica.Producto;                           // importa la entidad Producto que este controlador gestiona
import persistencias.exceptions.NonexistentEntityException; // excepción personalizada que se lanza cuando el registro no existe en BD

public class ProductoJpaController implements Serializable { // la clase implementa Serializable por requerimiento de JPA

    // ── CONSTRUCTORES ──────────────────────────────────────────────────────

    public ProductoJpaController(EntityManagerFactory emf) { // constructor que recibe la fábrica de conexiones desde afuera
        this.emf = emf;                                      // guarda la fábrica recibida en el atributo de la clase
    }

    private EntityManagerFactory emf = null; // atributo que almacena la fábrica de EntityManagers; null hasta que se asigne

    public EntityManager getEntityManager() {    // método que abre y retorna una sesión nueva con la BD
        return emf.createEntityManager();        // le pide a la fábrica que cree un EntityManager (abre la conexión)
    }

    public ProductoJpaController() {                         // constructor vacío que usan los servlets (new ProductoJpaController())
        this(JpaProvider.getEntityManagerFactory());         // llama al otro constructor pasando la fábrica del Singleton compartido
    }

    // ── CREATE ─────────────────────────────────────────────────────────────

    public void create(Producto producto) throws Exception { // método público que inserta un nuevo producto; puede lanzar Exception
        String sql = "INSERT INTO producto "                 // inicia el SQL INSERT sobre la tabla producto
                   + "(id_categoria, id_marca, nombre_producto, descripcion, " // columnas a insertar: las dos FK y los datos básicos
                   + " precio, stock, activo, imagen_url, created_at, updated_at) " // resto de columnas incluyendo fechas
                   + "VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10)"; // valores como parámetros posicionales del ?1 al ?10
        // no se incluye id_producto porque MySQL lo genera automáticamente con AUTO_INCREMENT

        EntityManager em = null;                             // declara em en null para verificarlo en el finally
        try {                                                // bloque protegido
            em = getEntityManager();                         // abre la sesión con la BD
            em.getTransaction().begin();                     // inicia la transacción; obligatorio antes de cualquier escritura

            if (producto.getCategoria() == null) throw new Exception("El producto debe tener una categoría."); // valida que la categoría no sea null porque id_categoria es NOT NULL en la BD
            if (producto.getMarca()     == null) throw new Exception("El producto debe tener una marca.");     // valida que la marca no sea null porque id_marca es NOT NULL en la BD

            Timestamp createdAt = producto.getCreatedAt() != null    // verifica si createdAt tiene valor en el objeto
                                  ? Timestamp.valueOf(producto.getCreatedAt()) // si sí: convierte LocalDateTime → Timestamp (formato que entiende MySQL DATETIME)
                                  : null;                            // si no: pasa null (MySQL guarda NULL en la columna)

            Timestamp updatedAt = producto.getUpdatedAt() != null    // verifica si updatedAt tiene valor en el objeto
                                  ? Timestamp.valueOf(producto.getUpdatedAt()) // si sí: convierte LocalDateTime → Timestamp
                                  : null;                            // si no: pasa null

            em.createNativeQuery(sql)                                // crea la consulta SQL nativa con el String sql definido arriba
              .setParameter(1,  producto.getCategoria().getIdCategoria()) // ?1 = id_categoria: lee el ID del objeto Categoria que tiene el producto
              .setParameter(2,  producto.getMarca().getIdMarca())         // ?2 = id_marca: lee el ID del objeto Marca que tiene el producto
              .setParameter(3,  producto.getNombreProducto())             // ?3 = nombre_producto: nombre del perfume
              .setParameter(4,  producto.getDescripcion())                // ?4 = descripcion: puede ser null si el producto no tiene descripción
              .setParameter(5,  producto.getPrecio())                     // ?5 = precio: BigDecimal; MySQL lo guarda como DECIMAL(10,2)
              .setParameter(6,  producto.getStock())                      // ?6 = stock: número de unidades disponibles en inventario
              .setParameter(7,  producto.isActivo())                      // ?7 = activo: true/false; MySQL lo guarda como 1 o 0 (TINYINT)
              .setParameter(8,  producto.getImagenUrl())                  // ?8 = imagen_url: ruta de la imagen; puede ser null
              .setParameter(9,  createdAt)                                // ?9 = created_at: fecha de creación; puede ser null
              .setParameter(10, updatedAt)                                // ?10 = updated_at: fecha de última modificación; puede ser null
              .executeUpdate();                                           // envía el INSERT a MySQL

            em.getTransaction().commit();                    // confirma la transacción: los datos quedan guardados en MySQL
        } finally {                                          // este bloque se ejecuta siempre, haya error o no
            if (em != null) { em.close(); }                 // cierra la sesión para devolver la conexión al pool
        }
    }

    // ── EDIT (UPDATE) ──────────────────────────────────────────────────────

    public void edit(Producto producto) throws NonexistentEntityException, Exception { // actualiza un producto existente
        String sql = "UPDATE producto "                      // inicia el SQL UPDATE sobre la tabla producto
                   + "SET id_categoria    = ?1, "            // actualiza la FK de categoría con el valor de ?1
                   + "    id_marca        = ?2, "            // actualiza la FK de marca con el valor de ?2
                   + "    nombre_producto = ?3, "            // actualiza el nombre del producto con el valor de ?3
                   + "    descripcion     = ?4, "            // actualiza la descripción con el valor de ?4
                   + "    precio          = ?5, "            // actualiza el precio con el valor de ?5
                   + "    stock           = ?6, "            // actualiza el stock con el valor de ?6
                   + "    activo          = ?7, "            // actualiza el estado activo con el valor de ?7
                   + "    imagen_url      = ?8, "            // actualiza la URL de la imagen con el valor de ?8
                   + "    updated_at      = ?9 "             // actualiza la fecha de modificación con el valor de ?9
                   + "WHERE id_producto   = ?10";            // condición: solo modifica la fila cuyo id_producto coincida con ?10

        EntityManager em = null;                             // declara em en null
        try {                                                // bloque protegido
            em = getEntityManager();                         // abre la sesión
            em.getTransaction().begin();                     // inicia la transacción

            if (producto.getCategoria() == null) throw new Exception("El producto debe tener una categoría."); // valida que la categoría no sea null
            if (producto.getMarca()     == null) throw new Exception("El producto debe tener una marca.");     // valida que la marca no sea null

            Timestamp ahora = Timestamp.valueOf(java.time.LocalDateTime.now()); // obtiene la fecha y hora actuales y las convierte a Timestamp para MySQL

            int filasAfectadas = (int) em.createNativeQuery(sql)             // crea la consulta UPDATE y guarda cuántas filas modificó
                .setParameter(1,  producto.getCategoria().getIdCategoria())  // ?1 = nuevo id_categoria
                .setParameter(2,  producto.getMarca().getIdMarca())          // ?2 = nuevo id_marca
                .setParameter(3,  producto.getNombreProducto())              // ?3 = nuevo nombre
                .setParameter(4,  producto.getDescripcion())                 // ?4 = nueva descripción
                .setParameter(5,  producto.getPrecio())                      // ?5 = nuevo precio DECIMAL(10,2)
                .setParameter(6,  producto.getStock())                       // ?6 = nuevo stock
                .setParameter(7,  producto.isActivo())                       // ?7 = nuevo estado activo
                .setParameter(8,  producto.getImagenUrl())                   // ?8 = nueva URL de imagen
                .setParameter(9,  ahora)                                     // ?9 = updated_at = fecha y hora de ahora
                .setParameter(10, producto.getIdProducto())                  // ?10 = clave primaria en el WHERE
                .executeUpdate();                                            // ejecuta el UPDATE en MySQL

            em.getTransaction().commit();                    // confirma los cambios en MySQL

            if (filasAfectadas == 0) {                       // si MySQL no encontró ninguna fila con ese id_producto...
                throw new NonexistentEntityException(        // ...lanza la excepción indicando que no existe
                    "The producto with id " + producto.getIdProducto() + " no longer exists.");
            }
        } catch (Exception ex) {                             // captura cualquier excepción dentro del try
            if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback(); // deshace la transacción para no dejar datos a medias
            throw ex;                                        // relanza la excepción para que el servlet la gestione
        } finally {                                          // siempre se ejecuta
            if (em != null) { em.close(); }                 // cierra la sesión
        }
    }

    // ── DESTROY (DELETE) ───────────────────────────────────────────────────

    public void destroy(int id) throws NonexistentEntityException { // elimina un producto por ID; lanza excepción si no existe
        String sql = "DELETE FROM producto WHERE id_producto = ?1"; // SQL que borra la fila cuyo id_producto coincida con ?1

        EntityManager em = null;                             // declara em en null
        try {                                                // bloque protegido
            em = getEntityManager();                         // abre la sesión
            em.getTransaction().begin();                     // inicia transacción antes del DELETE

            int filasAfectadas = (int) em.createNativeQuery(sql)  // crea la consulta DELETE y guarda cuántas filas eliminó
                                         .setParameter(1, id)     // ?1 = ID del producto a eliminar
                                         .executeUpdate();         // ejecuta el DELETE en MySQL

            em.getTransaction().commit();                    // confirma: la fila queda eliminada definitivamente en MySQL

            if (filasAfectadas == 0) {                       // si MySQL no encontró la fila con ese ID...
                throw new NonexistentEntityException(        // ...lanza la excepción indicando que ese ID no existía
                    "The producto with id " + id + " no longer exists.");
            }
        } catch (NonexistentEntityException ex) {            // captura solo la excepción de "no existe"
            if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback(); // deshace la transacción
            throw ex;                                        // relanza para que el servlet lo gestione
        } finally {                                          // siempre se ejecuta
            if (em != null) { em.close(); }                 // cierra la sesión
        }
    }

    // ── FIND ALL (SELECT *) ────────────────────────────────────────────────

    public List<Producto> findProductoEntities() {           // método público que retorna TODOS los productos sin filtro
        return findProductoEntities(true, -1, -1);           // llama al método privado con all=true (sin paginación; -1 ignora LIMIT y OFFSET)
    }

    public List<Producto> findProductoEntities(int maxResults, int firstResult) { // versión con paginación
        return findProductoEntities(false, maxResults, firstResult);               // llama al privado con all=false
    }

    @SuppressWarnings("unchecked")                           // suprime la advertencia del compilador por el cast sin verificar a List<Producto>
    private List<Producto> findProductoEntities(boolean all, int maxResults, int firstResult) { // método privado que construye y ejecuta el SELECT
        String sql = "SELECT * FROM producto ORDER BY id_producto ASC"; // SQL que trae todas las columnas de la tabla producto ordenadas por ID

        EntityManager em = getEntityManager();               // abre la sesión (lectura no necesita transacción)
        try {                                                // bloque protegido
            javax.persistence.Query q = em.createNativeQuery(sql, Producto.class); // crea la consulta nativa; Producto.class indica cómo mapear cada fila a un objeto Producto
            if (!all) {                                      // si se pidió paginación...
                q.setMaxResults(maxResults);                 // establece el LIMIT: cuántas filas como máximo traer
                q.setFirstResult(firstResult);               // establece el OFFSET: desde qué posición de la lista empezar
            }
            return q.getResultList();                        // ejecuta el SELECT y retorna la lista de objetos Producto mapeados
        } finally {                                          // siempre se ejecuta
            em.close();                                      // cierra la sesión
        }
    }

    // ── FIND BY ID ─────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")                           // suprime la advertencia del compilador
    public Producto findProducto(int id) {                   // método que busca y retorna un producto por su clave primaria
        String sql = "SELECT * FROM producto WHERE id_producto = ?1"; // SQL que busca la fila exacta con ese id_producto

        EntityManager em = getEntityManager();               // abre la sesión
        try {                                                // bloque protegido
            List<Producto> resultado = em.createNativeQuery(sql, Producto.class) // ejecuta el SELECT con el filtro por ID
                                         .setParameter(1, id)                    // ?1 = el ID que se busca
                                         .getResultList();                        // obtiene la lista (tendrá 0 o 1 elemento)
            return resultado.isEmpty() ? null : resultado.get(0); // si la lista está vacía retorna null; si tiene elemento retorna el primero
        } finally {                                          // siempre se ejecuta
            em.close();                                      // cierra la sesión
        }
    }

    // ── COUNT ──────────────────────────────────────────────────────────────

    public int getProductoCount() {                          // método que retorna el número total de productos en la tabla
        String sql = "SELECT COUNT(*) FROM producto";        // SQL que cuenta todas las filas de la tabla producto

        EntityManager em = getEntityManager();               // abre la sesión
        try {                                                // bloque protegido
            Object resultado = em.createNativeQuery(sql).getSingleResult(); // ejecuta el COUNT(*) y obtiene el único valor retornado como Object
            return ((Number) resultado).intValue();          // castea a Number (cubre Long y BigInteger) y convierte a int
        } finally {                                          // siempre se ejecuta
            em.close();                                      // cierra la sesión
        }
    }
}
