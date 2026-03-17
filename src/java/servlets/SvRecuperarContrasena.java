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

/**
 * SvRecuperarContrasena — Servlet de recuperación de contraseña sin envío de correo.
 * Implementa un flujo de 2 pasos vía AJAX desde olvide_contrasena.jsp:
 *   Paso 1 — POST accion=verificar: comprueba que el correo existe en BD y retorna el nombre del usuario.
 *   Paso 2 — POST accion=cambiar: valida la nueva contraseña y la actualiza directamente en BD.
 * GET: redirige a la página de olvide_contrasena.jsp.
 */
@WebServlet(name = "SvRecuperarContrasena", urlPatterns = {"/SvRecuperarContrasena"})
public class SvRecuperarContrasena extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        // Determinar la acción: "verificar" (paso 1) o "cambiar" (paso 2)
        String accion = request.getParameter("accion");
        String correo = request.getParameter("correo"); // correo del usuario que quiere recuperar su cuenta

        if (correo == null || correo.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"El correo es obligatorio\"}");
            return;
        }

        EntityManager em = null;
        try {
            em = JpaProvider.getEntityManagerFactory().createEntityManager();

            // Buscar un usuario activo con ese correo (búsqueda insensible a mayúsculas)
            TypedQuery<Usuario> q = em.createQuery(
                "SELECT u FROM Usuario u WHERE LOWER(u.correoUsuario) = LOWER(:c) AND u.activo = true",
                Usuario.class);
            q.setParameter("c", correo.trim());
            List<Usuario> res = q.getResultList();

            // Si no existe ninguna cuenta activa con ese correo, retornar error amigable
            if (res.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"No existe una cuenta activa con ese correo\"}");
                return;
            }

            Usuario usuario = res.get(0);

            if ("verificar".equals(accion)) {
                // PASO 1: confirmar que el correo existe y retornar el nombre del usuario
                // El frontend usa esto para mostrar "Bienvenido, [nombre]" antes del paso 2
                String nombre = (usuario.getCliente() != null)
                    ? usuario.getCliente().getNombreCompleto() // nombre del cliente
                    : "Administrador"; // si no tiene cliente, es un admin del sistema
                out.print("{\"ok\":true,\"nombre\":\"" + esc(nombre) + "\"}");

            } else if ("cambiar".equals(accion)) {
                // PASO 2: validar y actualizar la nueva contraseña
                String nueva     = request.getParameter("nueva");     // nueva contraseña deseada
                String confirmar = request.getParameter("confirmar"); // confirmación de la contraseña

                // Validación: campo no vacío
                if (nueva == null || nueva.isBlank()) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"La nueva contrase\\u00f1a es obligatoria\"}");
                    return;
                }
                // Validación: entre 8 y 20 caracteres
                if (nueva.length() < 8 || nueva.length() > 20) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"La contrase\\u00f1a debe tener entre 8 y 20 caracteres\"}");
                    return;
                }
                // Validación: al menos 1 letra y 1 número
                if (!nueva.matches(".*[a-zA-Z].*") || !nueva.matches(".*[0-9].*")) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"La contrase\\u00f1a debe contener al menos una letra y un n\\u00famero\"}");
                    return;
                }
                // Validación: las dos contraseñas deben ser iguales
                if (!nueva.equals(confirmar)) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"Las contrase\\u00f1as no coinciden\"}");
                    return;
                }

                // Actualizar la contraseña en BD (texto plano, según las convenciones del proyecto)
                em.getTransaction().begin();
                Usuario u = em.find(Usuario.class, usuario.getIdUsuario()); // recargar para la transacción
                u.setContrasena(nueva);
                u.setUpdatedAt(java.time.LocalDateTime.now());
                em.merge(u); // UPDATE en BD
                em.getTransaction().commit();

                out.print("{\"ok\":true}"); // el frontend muestra la pantalla de éxito

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
