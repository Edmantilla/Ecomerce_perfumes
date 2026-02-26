package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import logica.Usuario;
import persistencias.UsuarioJpaController;

@WebServlet(name = "SvUsuarios", urlPatterns = {"/SvUsuarios"})
public class SvUsuarios extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            UsuarioJpaController ctrl = new UsuarioJpaController();
            List<Usuario> usuarios = ctrl.findUsuarioEntities();

            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < usuarios.size(); i++) {
                Usuario u = usuarios.get(i);
                if (i > 0) sb.append(",");
                sb.append("{");
                sb.append("\"id\":").append(u.getIdUsuario()).append(",");
                sb.append("\"correo\":\"").append(escapeJson(u.getCorreoUsuario())).append("\",");
                sb.append("\"activo\":").append(u.isActivo()).append(",");
                sb.append("\"rol\":\"").append(u.getRol() != null ? escapeJson(u.getRol().getNombreRol()) : "").append("\",");
                sb.append("\"nombre\":\"").append(
                    u.getCliente() != null ? escapeJson(u.getCliente().getNombreCompleto()) : "Admin"
                ).append("\",");
                sb.append("\"registro\":\"").append(u.getCreatedAt() != null ? u.getCreatedAt().toString() : "").append("\"");
                sb.append("}");
            }
            sb.append("]");
            out.print(sb.toString());

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            String accion = request.getParameter("accion");
            if ("desactivar".equals(accion)) {
                int id = Integer.parseInt(request.getParameter("id"));
                UsuarioJpaController ctrl = new UsuarioJpaController();
                Usuario u = ctrl.findUsuario(id);
                if (u != null) {
                    u.setActivo(false);
                    ctrl.edit(u);
                }
                out.print("{\"ok\":true}");
            } else {
                out.print("{\"error\":\"Acción desconocida\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }
}
