package servlets;

import java.io.IOException;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import logica.Usuario;
import persistencias.JpaProvider;

/**
 * SvLogin — Servlet de autenticación.
 * Recibe correo y contraseña por POST, verifica las credenciales en la BD,
 * carga los permisos del usuario en sesión y redirige al panel admin o al perfil.
 */
@WebServlet(name = "SvLogin", urlPatterns = {"/SvLogin"})
public class SvLogin extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        // Leer los campos del formulario de login
        String correo     = request.getParameter("correo_electronico");
        String contrasena = request.getParameter("contrasena");

        // Validar que ninguno de los dos campos venga vacío
        if (correo == null || contrasena == null || correo.isBlank() || contrasena.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/vistas/perfil.jsp?error=" +
                java.net.URLEncoder.encode("Correo y contraseña son obligatorios.", "UTF-8"));
            return;
        }

        EntityManager em = null;
        try {
            em = JpaProvider.getEntityManagerFactory().createEntityManager();

            // Buscar en BD un usuario activo con ese correo (insensible a mayúsculas)
            TypedQuery<Usuario> q = em.createQuery(
                "SELECT u FROM Usuario u WHERE u.correoUsuario = :correo AND u.activo = true",
                Usuario.class
            );
            q.setParameter("correo", correo.toLowerCase().trim());
            List<Usuario> resultados = q.getResultList();

            // Si no se encontró ningún usuario activo con ese correo
            if (resultados.isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/vistas/perfil.jsp?error=" +
                    java.net.URLEncoder.encode("Correo o contraseña incorrectos.", "UTF-8"));
                return;
            }

            Usuario usuario = resultados.get(0);

            // Comparar contraseña en texto plano (sin hash)
            if (!usuario.getContrasena().equals(contrasena)) {
                response.sendRedirect(request.getContextPath() + "/vistas/perfil.jsp?error=" +
                    java.net.URLEncoder.encode("Correo o contraseña incorrectos.", "UTF-8"));
                return;
            }

            // Registrar la fecha y hora del último acceso del usuario
            em.getTransaction().begin();
            usuario.registrarAcceso();
            em.merge(usuario);
            em.getTransaction().commit();

            // Forzar carga de los datos del cliente en memoria antes de cerrar el EntityManager
            // (JPA usa carga lazy por defecto; si no se accede aquí, lanzaría LazyInitializationException después)
            if (usuario.getCliente() != null) {
                usuario.getCliente().getNombreCompleto();
                usuario.getCliente().getDireccion();
            }

            // Cargar todos los permisos activos del rol del usuario desde la BD
            // y construir la lista de nombres de permisos que se guardará en sesión
            java.util.List<String> nombresPermisos = new java.util.ArrayList<>();
            boolean puedeVerAdmin = usuario.esAdmin(); // admin puro siempre puede ver el panel
            if (usuario.getRol() != null) {
                usuario.getRol().getNombreRol(); // forzar carga eager del nombre del rol
                // Consulta JPQL que trae los rol_permiso junto con el permiso asociado (JOIN FETCH evita N+1)
                javax.persistence.TypedQuery<logica.Rolpermiso> qrp = em.createQuery(
                    "SELECT rp FROM Rolpermiso rp JOIN FETCH rp.permiso WHERE rp.rol.idRol = :id",
                    logica.Rolpermiso.class);
                qrp.setParameter("id", usuario.getRol().getIdRol());
                java.util.List<logica.Rolpermiso> rolPermisos = qrp.getResultList();
                for (logica.Rolpermiso rp : rolPermisos) {
                    // Solo agregar permisos que estén activos en la BD
                    if (rp.getPermiso() != null && rp.getPermiso().isActivo()) {
                        nombresPermisos.add(rp.getPermiso().getNombrePermiso());
                        // Si tiene VER_DASHBOARD, puede acceder al panel admin
                        if ("VER_DASHBOARD".equalsIgnoreCase(rp.getPermiso().getNombrePermiso())) {
                            puedeVerAdmin = true;
                        }
                    }
                }
            }

            // Crear la sesión HTTP y guardar los datos del usuario
            // Estos atributos son leídos por AuthHelper en cada request posterior
            HttpSession session = request.getSession(true);
            session.setAttribute("usuario", usuario);                          // objeto completo del usuario
            session.setAttribute("idUsuario", usuario.getIdUsuario());         // ID para consultas rápidas
            session.setAttribute("correoUsuario", usuario.getCorreoUsuario()); // correo para mostrar en UI
            session.setAttribute("esAdmin", usuario.esAdmin());                // flag para admin puro
            session.setAttribute(AuthHelper.SESS_PERMISOS, nombresPermisos);   // lista de permisos activos

            // Redirigir según el tipo de usuario
            if (puedeVerAdmin) {
                response.sendRedirect(request.getContextPath() + "/vistas/admin.jsp");
            } else {
                response.sendRedirect(request.getContextPath() + "/vistas/perfil.jsp");
            }

        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/vistas/perfil.jsp?error=" +
                java.net.URLEncoder.encode("Error al iniciar sesión. Intenta de nuevo.", "UTF-8"));
        } finally {
            if (em != null && em.isOpen()) em.close();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/vistas/perfil.jsp").forward(request, response);
    }
}
