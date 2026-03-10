package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import logica.Usuario;
import persistencias.JpaProvider;

@WebServlet(name = "SvRecuperarContrasena", urlPatterns = {"/SvRecuperarContrasena"})
public class SvRecuperarContrasena extends HttpServlet {

    /**
     * POST accion=verificar  correo -> verifica si existe, retorna JSON {ok, nombre}
     * POST accion=cambiar    correo, nueva, confirmar -> cambia la contraseña
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String accion = request.getParameter("accion");
        String correo = request.getParameter("correo");

        if (correo == null || correo.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"El correo es obligatorio\"}");
            return;
        }

        EntityManager em = null;
        try {
            em = JpaProvider.getEntityManagerFactory().createEntityManager();

            TypedQuery<Usuario> q = em.createQuery(
                "SELECT u FROM Usuario u WHERE LOWER(u.correoUsuario) = LOWER(:c) AND u.activo = true",
                Usuario.class);
            q.setParameter("c", correo.trim());
            List<Usuario> res = q.getResultList();

            if (res.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"No existe una cuenta activa con ese correo\"}");
                return;
            }

            Usuario usuario = res.get(0);

            if ("verificar".equals(accion)) {
                String nombre = (usuario.getCliente() != null)
                    ? usuario.getCliente().getNombreCompleto()
                    : "Administrador";
                out.print("{\"ok\":true,\"nombre\":\"" + esc(nombre) + "\"}");

            } else if ("cambiar".equals(accion)) {
                String nueva    = request.getParameter("nueva");
                String confirmar = request.getParameter("confirmar");

                if (nueva == null || nueva.isBlank()) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"La nueva contrase\\u00f1a es obligatoria\"}");
                    return;
                }
                if (nueva.length() < 6) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"La contrase\\u00f1a debe tener al menos 6 caracteres\"}");
                    return;
                }
                if (!nueva.equals(confirmar)) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"Las contrase\\u00f1as no coinciden\"}");
                    return;
                }

                em.getTransaction().begin();
                Usuario u = em.find(Usuario.class, usuario.getIdUsuario());
                u.setContrasena(nueva);
                u.setUpdatedAt(java.time.LocalDateTime.now());
                em.merge(u);
                em.getTransaction().commit();

                out.print("{\"ok\":true}");

            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Acci\\u00f3n desconocida\"}");
            }

        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"" + esc(e.getMessage()) + "\"}");
        } finally {
            if (em != null && em.isOpen()) em.close();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/vistas/olvide_contrasena.jsp");
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }
}
