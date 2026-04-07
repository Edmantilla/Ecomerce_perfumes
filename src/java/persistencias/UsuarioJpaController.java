package persistencias; // declara que esta clase pertenece al paquete "persistencias"

import java.io.Serializable;                     // permite que esta clase pueda convertirse en bytes (requerido por JPA)
import java.sql.Timestamp;                        // tipo de Java que representa fecha+hora; MySQL lo necesita para columnas DATETIME
import java.util.List;                            // permite usar listas (List<Usuario>) como tipo de retorno
import javax.persistence.EntityManager;           // clase de JPA que representa la sesión activa con la base de datos
import javax.persistence.EntityManagerFactory;    // fábrica que crea EntityManagers; se crea una sola vez en toda la app
import logica.Usuario;                            // importa la entidad Usuario que este controlador gestiona
import persistencias.exceptions.NonexistentEntityException; // excepción personalizada que se lanza cuando el registro no existe en BD

public class UsuarioJpaController implements Serializable { // la clase implementa Serializable por requerimiento de JPA

    // ── CONSTRUCTORES ──────────────────────────────────────────────────────

    public UsuarioJpaController(EntityManagerFactory emf) { // constructor que recibe la fábrica de conexiones desde afuera
        this.emf = emf;                                     // guarda la fábrica recibida en el atributo de la clase
    }

    private EntityManagerFactory emf = null; // atributo que almacena la fábrica de EntityManagers; null hasta que se asigne

    public EntityManager getEntityManager() {    // método que abre y retorna una sesión nueva con la BD
        return emf.createEntityManager();        // le pide a la fábrica que cree un EntityManager (abre la conexión)
    }

    public UsuarioJpaController() {                          // constructor vacío que usan los servlets (new UsuarioJpaController())
        this(JpaProvider.getEntityManagerFactory());         // llama al otro constructor pasando la fábrica del Singleton compartido
    }

    // ── CREATE ─────────────────────────────────────────────────────────────

    public void create(Usuario usuario) throws Exception {   // método público que inserta un nuevo usuario; puede lanzar Exception
        String sql = "INSERT INTO usuario "                  // inicia el SQL INSERT sobre la tabla usuario
                   + "(id_rol, id_cliente, correo_usuario, contrasena, activo, created_at, updated_at) " // columnas a insertar; id_usuario se omite porque MySQL lo genera con AUTO_INCREMENT
                   + "VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7)"; // valores como parámetros posicionales del ?1 al ?7
        // id_cliente puede ser NULL: si el usuario es admin puro no tiene fila en la tabla 'cliente'

        EntityManager em = null;                             // declara em en null para verificarlo en el finally
        try {                                                // bloque protegido
            em = getEntityManager();                         // abre la sesión con la BD
            em.getTransaction().begin();                     // inicia la transacción; obligatorio antes de cualquier escritura

            if (usuario.getRol() == null) throw new Exception("El usuario debe tener un rol."); // valida que el rol no sea null porque id_rol es NOT NULL en la BD

            Integer idCliente = (usuario.getCliente() != null)       // comprueba si el usuario tiene un cliente asociado
                                ? usuario.getCliente().getIdCliente() // si sí: extrae el ID del objeto Cliente
                                : null;                               // si no: null → se guardará NULL en la columna id_cliente (admin puro)

            Timestamp createdAt = usuario.getCreatedAt() != null      // verifica si createdAt tiene valor en el objeto
                                  ? Timestamp.valueOf(usuario.getCreatedAt()) // si sí: convierte LocalDateTime → Timestamp para MySQL
                                  : null;                             // si no: pasa null

            Timestamp updatedAt = usuario.getUpdatedAt() != null      // verifica si updatedAt tiene valor en el objeto
                                  ? Timestamp.valueOf(usuario.getUpdatedAt()) // si sí: convierte LocalDateTime → Timestamp
                                  : null;                             // si no: pasa null

            em.createNativeQuery(sql)                                // crea la consulta SQL nativa con el String sql definido arriba
              .setParameter(1, usuario.getRol().getIdRol())          // ?1 = id_rol: extrae el ID del objeto Rol que tiene el usuario
              .setParameter(2, idCliente)                            // ?2 = id_cliente: el Integer calculado arriba (puede ser null)
              .setParameter(3, usuario.getCorreoUsuario())           // ?3 = correo_usuario: correo único del usuario
              .setParameter(4, usuario.getContrasena())              // ?4 = contrasena: texto plano (sin hash, diseño actual del proyecto)
              .setParameter(5, usuario.isActivo())                   // ?5 = activo: true/false; MySQL lo guarda como 1 o 0
              .setParameter(6, createdAt)                            // ?6 = created_at: fecha de creación; puede ser null
              .setParameter(7, updatedAt)                            // ?7 = updated_at: fecha de modificación; puede ser null
              .executeUpdate();                                      // envía el INSERT a MySQL

            em.getTransaction().commit();                    // confirma la transacción: el usuario queda guardado en MySQL
        } finally {                                          // este bloque se ejecuta siempre, haya error o no
            if (em != null) { em.close(); }                 // cierra la sesión para devolver la conexión al pool
        }
    }

    // ── EDIT (UPDATE) ──────────────────────────────────────────────────────

    public void edit(Usuario usuario) throws NonexistentEntityException, Exception { // actualiza un usuario existente
        String sql = "UPDATE usuario "                       // inicia el SQL UPDATE sobre la tabla usuario
                   + "SET id_rol        = ?1, "              // actualiza la FK de rol con el valor de ?1
                   + "    id_cliente    = ?2, "              // actualiza la FK de cliente con el valor de ?2 (puede ser null)
                   + "    correo_usuario= ?3, "              // actualiza el correo con el valor de ?3
                   + "    contrasena    = ?4, "              // actualiza la contraseña con el valor de ?4
                   + "    activo        = ?5, "              // actualiza el estado activo con el valor de ?5
                   + "    updated_at    = ?6 "               // actualiza la fecha de modificación con el valor de ?6
                   + "WHERE id_usuario  = ?7";               // condición: solo modifica la fila cuyo id_usuario coincida con ?7

        EntityManager em = null;                             // declara em en null
        try {                                                // bloque protegido
            em = getEntityManager();                         // abre la sesión
            em.getTransaction().begin();                     // inicia la transacción

            if (usuario.getRol() == null) throw new Exception("El usuario debe tener un rol."); // valida que el rol no sea null

            Integer idCliente = (usuario.getCliente() != null)       // comprueba si el usuario tiene cliente asociado
                                ? usuario.getCliente().getIdCliente() // si sí: extrae el ID del objeto Cliente
                                : null;                               // si no: null → columna id_cliente quedará NULL (sigue siendo admin puro)

            Timestamp ahora = Timestamp.valueOf(java.time.LocalDateTime.now()); // obtiene la fecha y hora actuales y las convierte a Timestamp para MySQL

            int filasAfectadas = (int) em.createNativeQuery(sql)             // crea la consulta UPDATE y guarda cuántas filas modificó
                .setParameter(1, usuario.getRol().getIdRol())                // ?1 = nuevo id_rol
                .setParameter(2, idCliente)                                  // ?2 = nuevo id_cliente (puede ser null)
                .setParameter(3, usuario.getCorreoUsuario())                 // ?3 = nuevo correo
                .setParameter(4, usuario.getContrasena())                    // ?4 = nueva contraseña
                .setParameter(5, usuario.isActivo())                         // ?5 = nuevo estado activo
                .setParameter(6, ahora)                                      // ?6 = updated_at = fecha y hora de ahora
                .setParameter(7, usuario.getIdUsuario())                     // ?7 = clave primaria en el WHERE
                .executeUpdate();                                            // ejecuta el UPDATE en MySQL

            em.getTransaction().commit();                    // confirma los cambios en MySQL

            if (filasAfectadas == 0) {                       // si MySQL no encontró ninguna fila con ese id_usuario...
                throw new NonexistentEntityException(        // ...lanza la excepción indicando que ese usuario no existe
                    "The usuario with id " + usuario.getIdUsuario() + " no longer exists.");
            }
        } catch (Exception ex) {                             // captura cualquier excepción dentro del try
            if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback(); // deshace la transacción para no dejar datos a medias
            throw ex;                                        // relanza la excepción para que el servlet la gestione
        } finally {                                          // siempre se ejecuta
            if (em != null) { em.close(); }                 // cierra la sesión
        }
    }

    // ── DESTROY (DELETE) ───────────────────────────────────────────────────

    public void destroy(int id) throws NonexistentEntityException { // elimina un usuario por ID; lanza excepción si no existe
        String sql = "DELETE FROM usuario WHERE id_usuario = ?1"; // SQL que borra la fila cuyo id_usuario coincida con ?1

        EntityManager em = null;                             // declara em en null
        try {                                                // bloque protegido
            em = getEntityManager();                         // abre la sesión
            em.getTransaction().begin();                     // inicia transacción antes del DELETE

            int filasAfectadas = (int) em.createNativeQuery(sql)  // crea la consulta DELETE y guarda cuántas filas eliminó
                                         .setParameter(1, id)     // ?1 = ID del usuario a eliminar
                                         .executeUpdate();         // ejecuta el DELETE en MySQL

            em.getTransaction().commit();                    // confirma: la fila queda eliminada definitivamente en MySQL

            if (filasAfectadas == 0) {                       // si MySQL no encontró la fila con ese ID...
                throw new NonexistentEntityException(        // ...lanza la excepción indicando que ese ID no existía
                    "The usuario with id " + id + " no longer exists.");
            }
        } catch (NonexistentEntityException ex) {            // captura solo la excepción de "no existe"
            if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback(); // deshace la transacción
            throw ex;                                        // relanza para que el servlet lo gestione
        } finally {                                          // siempre se ejecuta
            if (em != null) { em.close(); }                 // cierra la sesión
        }
    }

    // ── FIND ALL (SELECT *) ────────────────────────────────────────────────

    public List<Usuario> findUsuarioEntities() {             // método público que retorna TODOS los usuarios sin filtro
        return findUsuarioEntities(true, -1, -1);            // llama al método privado con all=true (sin paginación; -1 ignora LIMIT y OFFSET)
    }

    public List<Usuario> findUsuarioEntities(int maxResults, int firstResult) { // versión con paginación
        return findUsuarioEntities(false, maxResults, firstResult);              // llama al privado con all=false
    }

    @SuppressWarnings("unchecked")                           // suprime la advertencia del compilador por el cast sin verificar a List<Usuario>
    private List<Usuario> findUsuarioEntities(boolean all, int maxResults, int firstResult) { // método privado que construye y ejecuta el SELECT
        String sql = "SELECT * FROM usuario ORDER BY id_usuario ASC"; // SQL que trae todas las columnas de la tabla usuario ordenadas por ID

        EntityManager em = getEntityManager();               // abre la sesión (lectura no necesita transacción)
        try {                                                // bloque protegido
            javax.persistence.Query q = em.createNativeQuery(sql, Usuario.class); // crea la consulta nativa; Usuario.class indica cómo mapear cada fila a un objeto Usuario
            if (!all) {                                      // si se pidió paginación...
                q.setMaxResults(maxResults);                 // establece el LIMIT: cuántas filas como máximo traer
                q.setFirstResult(firstResult);               // establece el OFFSET: desde qué posición de la lista empezar
            }
            return q.getResultList();                        // ejecuta el SELECT y retorna la lista de objetos Usuario mapeados
        } finally {                                          // siempre se ejecuta
            em.close();                                      // cierra la sesión
        }
    }

    // ── FIND BY ID ─────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")                           // suprime la advertencia del compilador
    public Usuario findUsuario(int id) {                     // método que busca y retorna un usuario por su clave primaria
        String sql = "SELECT * FROM usuario WHERE id_usuario = ?1"; // SQL que busca la fila exacta con ese id_usuario

        EntityManager em = getEntityManager();               // abre la sesión
        try {                                                // bloque protegido
            List<Usuario> resultado = em.createNativeQuery(sql, Usuario.class) // ejecuta el SELECT con el filtro por ID
                                        .setParameter(1, id)                   // ?1 = el ID que se busca
                                        .getResultList();                       // obtiene la lista (tendrá 0 o 1 elemento)
            return resultado.isEmpty() ? null : resultado.get(0); // si la lista está vacía retorna null; si tiene elemento retorna el primero
        } finally {                                          // siempre se ejecuta
            em.close();                                      // cierra la sesión
        }
    }

    // ── COUNT ──────────────────────────────────────────────────────────────

    public int getUsuarioCount() {                           // método que retorna el número total de usuarios en la tabla
        String sql = "SELECT COUNT(*) FROM usuario";         // SQL que cuenta todas las filas de la tabla usuario

        EntityManager em = getEntityManager();               // abre la sesión
        try {                                                // bloque protegido
            Object resultado = em.createNativeQuery(sql).getSingleResult(); // ejecuta el COUNT(*) y obtiene el único valor retornado como Object
            return ((Number) resultado).intValue();          // castea a Number (cubre Long y BigInteger) y convierte a int
        } finally {                                          // siempre se ejecuta
            em.close();                                      // cierra la sesión
        }
    }
}
