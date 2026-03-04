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
import logica.Telefonocliente;
import logica.Correocliente;
import logica.Usuario;
import persistencias.JpaProvider;
import persistencias.UsuarioJpaController;

@WebServlet(name = "SvUsuarios", urlPatterns = {"/SvUsuarios"})
public class SvUsuarios extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        EntityManager em = null;
        try {
            if (!AuthHelper.tienePermiso(request, "VER_USUARIOS")) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print("{\"error\":\"Sin permiso: VER_USUARIOS\"}");
                return;
            }

            UsuarioJpaController ctrl = new UsuarioJpaController();
            List<Usuario> usuarios = ctrl.findUsuarioEntities();
            em = JpaProvider.getEntityManagerFactory().createEntityManager();

            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < usuarios.size(); i++) {
                Usuario u = usuarios.get(i);
                if (i > 0) sb.append(",");
                sb.append("{");
                sb.append("\"id\":").append(u.getIdUsuario()).append(",");
                sb.append("\"correo\":\"").append(escapeJson(u.getCorreoUsuario())).append("\",");
                sb.append("\"activo\":").append(u.isActivo()).append(",");
                sb.append("\"rol\":\"").append(u.getRol() != null ? escapeJson(u.getRol().getNombreRol()) : "").append("\",");
                sb.append("\"idRol\":").append(u.getRol() != null ? u.getRol().getIdRol() : 0).append(",");
                sb.append("\"nombre\":\"").append(
                    u.getCliente() != null ? escapeJson(u.getCliente().getNombreCompleto()) : "Admin"
                ).append("\",");
                sb.append("\"registro\":\"").append(u.getCreatedAt() != null ? u.getCreatedAt().toString() : "").append("\",");

                if (u.getCliente() != null) {
                    int idC = u.getCliente().getIdCliente();
                    // Dirección
                    String dir = u.getCliente().getDireccion();
                    sb.append("\"direccion\":\"").append(escapeJson(dir != null ? dir : "")).append("\",");

                    // Teléfonos activos
                    List<Telefonocliente> tels = em.createQuery(
                        "SELECT t FROM Telefonocliente t WHERE t.cliente.idCliente = :id AND t.activo = true ORDER BY t.idTelefono",
                        Telefonocliente.class).setParameter("id", idC).getResultList();
                    sb.append("\"telefonos\":[");
                    for (int j = 0; j < tels.size(); j++) {
                        if (j > 0) sb.append(",");
                        sb.append("{\"numero\":\"").append(escapeJson(tels.get(j).getTelefono())).append("\",");
                        sb.append("\"tipo\":\"").append(tels.get(j).getTipoTelefono() != null ? tels.get(j).getTipoTelefono().name() : "").append("\"}");
                    }
                    sb.append("],");

                    // Correos adicionales activos
                    List<Correocliente> correos = em.createQuery(
                        "SELECT c FROM Correocliente c WHERE c.cliente.idCliente = :id AND c.activo = true ORDER BY c.idCorreo",
                        Correocliente.class).setParameter("id", idC).getResultList();
                    sb.append("\"correosAdicionales\":[");
                    for (int j = 0; j < correos.size(); j++) {
                        if (j > 0) sb.append(",");
                        sb.append("{\"correo\":\"").append(escapeJson(correos.get(j).getCorreo())).append("\",");
                        sb.append("\"principal\":").append(correos.get(j).isPrincipal()).append("}");
                    }
                    sb.append("],");

                    // Conteo de pedidos
                    Long numPedidos = em.createQuery(
                        "SELECT COUNT(p) FROM Pedido p WHERE p.cliente.idCliente = :id", Long.class)
                        .setParameter("id", idC).getSingleResult();
                    sb.append("\"numPedidos\":").append(numPedidos);
                } else {
                    sb.append("\"direccion\":\"\",\"telefonos\":[],\"correosAdicionales\":[],\"numPedidos\":0");
                }

                sb.append("}");
            }
            sb.append("]");
            out.print(sb.toString());

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        } finally {
            if (em != null && em.isOpen()) em.close();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            if (!AuthHelper.tienePermiso(request, "EDITAR_USUARIOS")) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print("{\"error\":\"Sin permiso: EDITAR_USUARIOS\"}");
                return;
            }

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
            } else if ("cambiarRol".equals(accion)) {
                int idUsuario = Integer.parseInt(request.getParameter("id"));
                int idRol     = Integer.parseInt(request.getParameter("idRol"));
                EntityManager em = JpaProvider.getEntityManagerFactory().createEntityManager();
                try {
                    em.getTransaction().begin();
                    Usuario u = em.find(Usuario.class, idUsuario);
                    if (u == null) { out.print("{\"error\":\"Usuario no encontrado\"}"); return; }
                    logica.Rol nuevoRol = em.find(logica.Rol.class, idRol);
                    if (nuevoRol == null) { out.print("{\"error\":\"Rol no encontrado\"}"); return; }
                    u.setRol(nuevoRol);
                    u.setUpdatedAt(java.time.LocalDateTime.now());
                    em.merge(u);
                    em.getTransaction().commit();
                    out.print("{\"ok\":true,\"rol\":\"" + escapeJson(nuevoRol.getNombreRol()) + "\"}");
                } finally {
                    if (em.isOpen()) em.close();
                }
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
