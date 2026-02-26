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

@WebServlet(name = "SvLogin", urlPatterns = {"/SvLogin"})
public class SvLogin extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String correo     = request.getParameter("correo_electronico");
        String contrasena = request.getParameter("contrasena");

        if (correo == null || contrasena == null || correo.isBlank() || contrasena.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/vistas/perfil.jsp?error=" +
                java.net.URLEncoder.encode("Correo y contraseña son obligatorios.", "UTF-8"));
            return;
        }

        EntityManager em = null;
        try {
            em = JpaProvider.getEntityManagerFactory().createEntityManager();

            TypedQuery<Usuario> q = em.createQuery(
                "SELECT u FROM Usuario u WHERE u.correoUsuario = :correo AND u.activo = true",
                Usuario.class
            );
            q.setParameter("correo", correo.toLowerCase().trim());
            List<Usuario> resultados = q.getResultList();

            if (resultados.isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/vistas/perfil.jsp?error=" +
                    java.net.URLEncoder.encode("Correo o contraseña incorrectos.", "UTF-8"));
                return;
            }

            Usuario usuario = resultados.get(0);

            if (!usuario.getContrasena().equals(contrasena)) {
                response.sendRedirect(request.getContextPath() + "/vistas/perfil.jsp?error=" +
                    java.net.URLEncoder.encode("Correo o contraseña incorrectos.", "UTF-8"));
                return;
            }

            em.getTransaction().begin();
            usuario.registrarAcceso();
            em.merge(usuario);
            em.getTransaction().commit();

            // Forzar carga eager del cliente y rol antes de cerrar el EntityManager
            if (usuario.getCliente() != null) {
                usuario.getCliente().getNombreCompleto();
                usuario.getCliente().getDireccion();
            }
            if (usuario.getRol() != null) {
                usuario.getRol().getNombreRol();
            }

            HttpSession session = request.getSession(true);
            session.setAttribute("usuario", usuario);
            session.setAttribute("idUsuario", usuario.getIdUsuario());
            session.setAttribute("correoUsuario", usuario.getCorreoUsuario());
            session.setAttribute("esAdmin", usuario.esAdmin());

            if (usuario.esAdmin()) {
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
