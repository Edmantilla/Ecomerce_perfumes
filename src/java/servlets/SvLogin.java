package servlets; // declara que esta clase pertenece al paquete "servlets"

import java.io.IOException;                          // excepción de entrada/salida (requerida por doPost/doGet)
import java.util.List;                               // lista de resultados de la consulta JPQL
import javax.persistence.EntityManager;              // gestiona la sesión activa con la BD
import javax.persistence.TypedQuery;                 // consulta JPQL con tipo de retorno definido
import javax.servlet.ServletException;               // excepción propia de los servlets
import javax.servlet.annotation.WebServlet;          // anotación que mapea la URL al servlet
import javax.servlet.http.HttpServlet;               // clase base de todos los servlets HTTP
import javax.servlet.http.HttpServletRequest;        // representa la petición HTTP entrante
import javax.servlet.http.HttpServletResponse;       // representa la respuesta HTTP saliente
import javax.servlet.http.HttpSession;               // maneja la sesión HTTP del usuario
import logica.Usuario;                               // entidad del usuario para la consulta y la sesión
import persistencias.JpaProvider;                    // singleton que provee la EntityManagerFactory

@WebServlet(name = "SvLogin", urlPatterns = {"/SvLogin"}) // cuando llegue una petición a /SvLogin, Tomcat ejecuta esta clase
public class SvLogin extends HttpServlet {           // extiende HttpServlet para manejar peticiones HTTP

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) // se ejecuta cuando el formulario de login envía POST
            throws ServletException, IOException {   // declara las excepciones que puede lanzar

        request.setCharacterEncoding("UTF-8");       // fuerza UTF-8 para leer correctamente caracteres especiales del formulario

        String correo     = request.getParameter("correo_electronico"); // lee el campo "correo_electronico" del formulario HTML
        String contrasena = request.getParameter("contrasena");         // lee el campo "contrasena" del formulario HTML

        if (correo == null || contrasena == null || correo.isBlank() || contrasena.isBlank()) { // valida que ningún campo venga vacío o nulo
            response.sendRedirect(request.getContextPath() + "/vistas/perfil.jsp?error=" + // redirige a perfil.jsp con el mensaje de error como parámetro URL
                java.net.URLEncoder.encode("Correo y contraseña son obligatorios.", "UTF-8")); // codifica el mensaje para que sea válido en una URL
            return;                                  // detiene la ejecución del método, la respuesta ya fue enviada
        }

        EntityManager em = null;                     // declara em en null para poder cerrarlo en el finally aunque falle la apertura
        try {                                        // bloque protegido: si algo falla se captura abajo
            em = JpaProvider.getEntityManagerFactory().createEntityManager(); // abre una sesión con la BD usando la fábrica Singleton

            TypedQuery<Usuario> q = em.createQuery(  // crea una consulta JPQL tipada que retorna objetos Usuario
                "SELECT u FROM Usuario u WHERE u.correoUsuario = :correo AND u.activo = true", // busca usuario activo con ese correo
                Usuario.class                        // indica que cada fila se mapea a un objeto Usuario
            );
            q.setParameter("correo", correo.toLowerCase().trim()); // reemplaza :correo con el correo normalizado (minúsculas, sin espacios)
            List<Usuario> resultados = q.getResultList(); // ejecuta el SELECT y obtiene la lista (0 o 1 elemento esperado)

            if (resultados.isEmpty()) {              // si la lista está vacía, no existe usuario activo con ese correo
                response.sendRedirect(request.getContextPath() + "/vistas/perfil.jsp?error=" +
                    java.net.URLEncoder.encode("Correo o contraseña incorrectos.", "UTF-8")); // mensaje genérico para no revelar si el correo existe
                return;                              // detiene el método
            }

            Usuario usuario = resultados.get(0);     // toma el primer (y único) usuario de la lista

            if (!usuario.getContrasena().equals(contrasena)) { // compara la contraseña ingresada con la almacenada en texto plano
                response.sendRedirect(request.getContextPath() + "/vistas/perfil.jsp?error=" +
                    java.net.URLEncoder.encode("Correo o contraseña incorrectos.", "UTF-8")); // mismo mensaje para no revelar cuál campo falló
                return;                              // detiene el método
            }

            em.getTransaction().begin();             // inicia transacción para poder actualizar el último acceso
            usuario.registrarAcceso();               // llama al método de la entidad que setea ultimoAcceso y updatedAt = LocalDateTime.now()
            em.merge(usuario);                       // UPDATE usuario SET ultimo_acceso=?, updated_at=? WHERE id_usuario=?
            em.getTransaction().commit();            // confirma el UPDATE en la BD

            if (usuario.getCliente() != null) {      // verifica si el usuario tiene un cliente asociado (si no, es admin puro)
                usuario.getCliente().getNombreCompleto(); // fuerza la carga lazy del nombre del cliente antes de cerrar el EntityManager
                usuario.getCliente().getDireccion();  // fuerza la carga lazy de la dirección, para que no falle al acceder después en la sesión
            }

            java.util.List<String> nombresPermisos = new java.util.ArrayList<>(); // lista que almacenará los nombres de los permisos del usuario
            boolean puedeVerAdmin = usuario.esAdmin(); // si es admin puro (sin cliente), siempre puede ver el panel admin
            if (usuario.getRol() != null) {          // verifica que el usuario tenga un rol asignado
                usuario.getRol().getNombreRol();      // fuerza la carga lazy del nombre del rol para tenerlo disponible en sesión
                javax.persistence.TypedQuery<logica.Rolpermiso> qrp = em.createQuery( // consulta JPQL que trae los permisos del rol del usuario
                    "SELECT rp FROM Rolpermiso rp JOIN FETCH rp.permiso WHERE rp.rol.idRol = :id", // JOIN FETCH evita el problema N+1 (carga todos los permisos en una sola consulta)
                    logica.Rolpermiso.class);         // cada fila se mapea a un objeto Rolpermiso
                qrp.setParameter("id", usuario.getRol().getIdRol()); // filtra por el ID del rol del usuario
                java.util.List<logica.Rolpermiso> rolPermisos = qrp.getResultList(); // obtiene las asignaciones rol-permiso
                for (logica.Rolpermiso rp : rolPermisos) { // itera sobre cada asignación
                    if (rp.getPermiso() != null && rp.getPermiso().isActivo()) { // solo agrega permisos que existan y estén activos
                        nombresPermisos.add(rp.getPermiso().getNombrePermiso()); // agrega el nombre del permiso a la lista (ej: "VER_PRODUCTOS")
                        if ("VER_DASHBOARD".equalsIgnoreCase(rp.getPermiso().getNombrePermiso())) { // si tiene el permiso VER_DASHBOARD...
                            puedeVerAdmin = true;     // ...puede acceder al panel de administración
                        }
                    }
                }
            }

            HttpSession session = request.getSession(true); // crea una sesión HTTP nueva (o retorna la existente si ya hay una)
            session.setAttribute("usuario", usuario);                          // guarda el objeto Usuario completo; usado por los JSP para leer datos
            session.setAttribute("idUsuario", usuario.getIdUsuario());         // guarda el ID por separado para consultas rápidas sin castear
            session.setAttribute("correoUsuario", usuario.getCorreoUsuario()); // guarda el correo para mostrarlo en la interfaz
            session.setAttribute("esAdmin", usuario.esAdmin());                // guarda el flag: true si no tiene cliente (admin puro)
            session.setAttribute(AuthHelper.SESS_PERMISOS, nombresPermisos);   // guarda la lista de nombres de permisos; AuthHelper la lee en cada request

            if (puedeVerAdmin) {                     // si puede ver el admin (admin puro o tiene VER_DASHBOARD)...
                response.sendRedirect(request.getContextPath() + "/vistas/admin.jsp"); // ...redirige al panel de administración
            } else {
                response.sendRedirect(request.getContextPath() + "/vistas/perfil.jsp"); // ...si no, redirige al perfil del cliente
            }

        } catch (Exception e) {                      // captura cualquier excepción inesperada (BD caída, etc.)
            response.sendRedirect(request.getContextPath() + "/vistas/perfil.jsp?error=" +
                java.net.URLEncoder.encode("Error al iniciar sesión. Intenta de nuevo.", "UTF-8")); // mensaje genérico al usuario
        } finally {
            if (em != null && em.isOpen()) em.close(); // SIEMPRE cierra el EntityManager para devolver la conexión al pool
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) // se ejecuta cuando alguien entra a /SvLogin por URL directa (GET)
            throws ServletException, IOException {
        request.getRequestDispatcher("/vistas/perfil.jsp").forward(request, response); // reenvía la petición al JSP de perfil/login para que se muestre el formulario
    }
}
