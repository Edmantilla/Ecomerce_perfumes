package persistencias; // declara que esta clase pertenece al paquete "persistencias"

import java.io.Serializable;                     // permite que esta clase pueda convertirse en bytes (requerido por JPA)
import java.util.List;                            // permite usar listas (List<Marca>) como tipo de retorno
import javax.persistence.EntityManager;           // clase de JPA que representa la sesión activa con la base de datos
import javax.persistence.EntityManagerFactory;    // fábrica que crea EntityManagers; se crea una sola vez en toda la app
import logica.Marca;                              // importa la entidad Marca que este controlador gestiona
import persistencias.exceptions.NonexistentEntityException; // excepción personalizada que se lanza cuando el registro no existe en BD

public class MarcaJpaController implements Serializable { // la clase implementa Serializable por requerimiento de JPA

    // ── CONSTRUCTORES ──────────────────────────────────────────────────────

    public MarcaJpaController(EntityManagerFactory emf) { // constructor que recibe la fábrica de conexiones desde afuera
        this.emf = emf;                                   // guarda la fábrica recibida en el atributo de la clase
    }

    private EntityManagerFactory emf = null; // atributo que almacena la fábrica de EntityManagers; null hasta que se asigne

    public EntityManager getEntityManager() {    // método que abre y retorna una sesión nueva con la BD
        return emf.createEntityManager();        // le pide a la fábrica que cree un EntityManager (abre la conexión)
    }

    public MarcaJpaController() {                        // constructor vacío que usan los servlets (new MarcaJpaController())
        this(JpaProvider.getEntityManagerFactory());     // llama al otro constructor pasando la fábrica del Singleton compartido
    }

    // ── CREATE ─────────────────────────────────────────────────────────────

    public void create(Marca marca) throws Exception {   // método público que inserta una nueva marca; puede lanzar Exception
        String sql = "INSERT INTO marca (nombre_marca, descripcion, genero, pagina_url, activo) " // primera parte del SQL: columnas a insertar
                   + "VALUES (?1, ?2, ?3, ?4, ?5)";     // segunda parte: valores como parámetros posicionales (?1 al ?5)
        // no se incluye id_marca porque MySQL lo genera automáticamente con AUTO_INCREMENT

        EntityManager em = null;                         // declara la variable em en null para poder verificarla en el finally
        try {                                            // bloque try: si algo falla, el finally siempre cierra la conexión
            em = getEntityManager();                     // abre la sesión con la BD llamando al método getEntityManager()
            em.getTransaction().begin();                 // inicia la transacción; sin esto MySQL (InnoDB) no guarda nada

            em.createNativeQuery(sql)                    // crea la consulta SQL nativa usando el String sql definido arriba
              .setParameter(1, marca.getNombreMarca())   // reemplaza ?1 con el nombre de la marca (ej: "Chanel")
              .setParameter(2, marca.getDescripcion())   // reemplaza ?2 con la descripción; puede ser null si no tiene
              .setParameter(3, marca.getGenero())        // reemplaza ?3 con el género: "HOMBRE" o "MUJER"
              .setParameter(4, marca.getPaginaUrl())     // reemplaza ?4 con la ruta del JSP (ej: "Chanel.jsp")
              .setParameter(5, marca.isActivo())         // reemplaza ?5 con true o false; MySQL lo guarda como 1 o 0
              .executeUpdate();                          // envía el INSERT a MySQL; retorna el número de filas insertadas

            em.getTransaction().commit();                // confirma la transacción: los datos quedan guardados definitivamente en MySQL
        } finally {                                      // este bloque se ejecuta siempre, haya error o no
            if (em != null) { em.close(); }             // cierra la sesión para devolver la conexión al pool de EclipseLink
        }
    }

    // ── EDIT (UPDATE) ──────────────────────────────────────────────────────

    public void edit(Marca marca) throws NonexistentEntityException, Exception { // método que actualiza una marca; lanza excepción si no existe
        String sql = "UPDATE marca "                     // inicia el SQL UPDATE sobre la tabla marca
                   + "SET nombre_marca = ?1, "           // actualiza la columna nombre_marca con el valor del ?1
                   + "    descripcion  = ?2, "           // actualiza descripcion con el valor del ?2
                   + "    genero       = ?3, "           // actualiza genero con el valor del ?3
                   + "    pagina_url   = ?4, "           // actualiza pagina_url con el valor del ?4
                   + "    activo       = ?5 "            // actualiza activo con el valor del ?5
                   + "WHERE id_marca   = ?6";            // condición: solo modifica la fila cuyo id_marca coincide con ?6

        EntityManager em = null;                         // declara em en null para controlarlo en el finally
        try {                                            // inicia el bloque protegido
            em = getEntityManager();                     // abre la sesión con la BD
            em.getTransaction().begin();                 // inicia la transacción antes de ejecutar el UPDATE

            int filasAfectadas = (int) em.createNativeQuery(sql)  // crea la consulta nativa y guarda en filasAfectadas cuántas filas tocó
                .setParameter(1, marca.getNombreMarca())           // ?1 = nuevo nombre de la marca
                .setParameter(2, marca.getDescripcion())           // ?2 = nueva descripción
                .setParameter(3, marca.getGenero())                // ?3 = nuevo género
                .setParameter(4, marca.getPaginaUrl())             // ?4 = nueva ruta JSP
                .setParameter(5, marca.isActivo())                 // ?5 = nuevo estado activo/inactivo
                .setParameter(6, marca.getIdMarca())               // ?6 = ID de la fila a modificar (va en el WHERE)
                .executeUpdate();                                   // ejecuta el UPDATE en MySQL

            em.getTransaction().commit();                // confirma la transacción; los cambios quedan guardados

            if (filasAfectadas == 0) {                   // si MySQL no encontró ninguna fila con ese id_marca...
                throw new NonexistentEntityException(    // ...lanza la excepción personalizada indicando que no existe
                    "The marca with id " + marca.getIdMarca() + " no longer exists."); // mensaje con el ID que falló
            }
        } catch (Exception ex) {                         // captura cualquier excepción que ocurra dentro del try
            if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback(); // si la transacción sigue abierta, la deshace para no dejar datos a medias
            throw ex;                                    // relanza la excepción para que el servlet la gestione
        } finally {                                      // se ejecuta siempre
            if (em != null) { em.close(); }             // cierra la sesión con la BD
        }
    }

    // ── DESTROY (DELETE) ───────────────────────────────────────────────────

    public void destroy(int id) throws NonexistentEntityException { // método que elimina una marca por su ID; lanza excepción si no existe
        String sql = "DELETE FROM marca WHERE id_marca = ?1"; // SQL que borra la fila cuyo id_marca coincida con ?1

        EntityManager em = null;                         // declara em en null
        try {                                            // bloque protegido
            em = getEntityManager();                     // abre la sesión
            em.getTransaction().begin();                 // inicia transacción antes del DELETE

            int filasAfectadas = (int) em.createNativeQuery(sql)  // crea la consulta DELETE y guarda cuántas filas eliminó
                                         .setParameter(1, id)     // ?1 = ID de la marca a eliminar
                                         .executeUpdate();         // ejecuta el DELETE en MySQL

            em.getTransaction().commit();                // confirma: la fila queda eliminada definitivamente

            if (filasAfectadas == 0) {                   // si MySQL no encontró ninguna fila con ese ID...
                throw new NonexistentEntityException(    // ...lanza la excepción indicando que ese ID no existía
                    "The marca with id " + id + " no longer exists.");
            }
        } catch (NonexistentEntityException ex) {        // captura solo la excepción de "no existe"
            if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback(); // deshace la transacción
            throw ex;                                    // relanza para que el servlet lo gestione
        } finally {                                      // siempre se ejecuta
            if (em != null) { em.close(); }             // cierra la sesión
        }
    }

    // ── FIND ALL (SELECT *) ────────────────────────────────────────────────

    public List<Marca> findMarcaEntities() {             // método público que retorna TODAS las marcas sin filtro
        return findMarcaEntities(true, -1, -1);          // llama al método privado con all=true (sin paginación)
    }

    public List<Marca> findMarcaEntities(int maxResults, int firstResult) { // versión con paginación: cuántas filas y desde cuál
        return findMarcaEntities(false, maxResults, firstResult);            // llama al método privado con all=false
    }

    @SuppressWarnings("unchecked")                       // suprime la advertencia del compilador por el cast sin verificar a List<Marca>
    private List<Marca> findMarcaEntities(boolean all, int maxResults, int firstResult) { // método privado que hace el trabajo real
        String sql = "SELECT * FROM marca ORDER BY id_marca ASC"; // SQL que trae todas las columnas de la tabla marca, ordenadas por ID

        EntityManager em = getEntityManager();           // abre la sesión con la BD (no necesita transacción porque es solo lectura)
        try {                                            // bloque protegido
            javax.persistence.Query q = em.createNativeQuery(sql, Marca.class); // crea la consulta nativa; el segundo argumento Marca.class le dice a JPA cómo mapear cada fila a un objeto Marca
            if (!all) {                                  // si se pidió paginación (all = false)...
                q.setMaxResults(maxResults);             // ...establece el LIMIT: cuántas filas como máximo traer
                q.setFirstResult(firstResult);           // ...establece el OFFSET: desde qué posición empezar
            }
            return q.getResultList();                    // ejecuta el SELECT y retorna la lista de objetos Marca
        } finally {                                      // siempre se ejecuta
            em.close();                                  // cierra la sesión
        }
    }

    // ── FIND BY ID (SELECT WHERE id) ───────────────────────────────────────

    @SuppressWarnings("unchecked")                       // suprime la advertencia del compilador por el cast sin verificar
    public Marca findMarca(int id) {                     // método que busca y retorna una marca por su clave primaria
        String sql = "SELECT * FROM marca WHERE id_marca = ?1"; // SQL que busca la fila exacta con ese ID

        EntityManager em = getEntityManager();           // abre la sesión
        try {                                            // bloque protegido
            List<Marca> resultado = em.createNativeQuery(sql, Marca.class) // ejecuta el SELECT y mapea el resultado a objetos Marca
                                      .setParameter(1, id)                 // ?1 = el ID que se busca
                                      .getResultList();                    // obtiene la lista (puede tener 0 o 1 elemento)
            return resultado.isEmpty() ? null : resultado.get(0); // si la lista está vacía retorna null; si tiene elemento retorna el primero
        } finally {                                      // siempre se ejecuta
            em.close();                                  // cierra la sesión
        }
    }

    // ── COUNT (SELECT COUNT(*)) ────────────────────────────────────────────

    public int getMarcaCount() {                         // método que retorna el número total de marcas en la tabla
        String sql = "SELECT COUNT(*) FROM marca";       // SQL que cuenta todas las filas de la tabla marca

        EntityManager em = getEntityManager();           // abre la sesión
        try {                                            // bloque protegido
            Object resultado = em.createNativeQuery(sql).getSingleResult(); // ejecuta el COUNT(*) y obtiene el único valor retornado
            return ((Number) resultado).intValue();      // castea a Number (cubre Long y BigInteger según el driver) y convierte a int
        } finally {                                      // siempre se ejecuta
            em.close();                                  // cierra la sesión
        }
    }
}
