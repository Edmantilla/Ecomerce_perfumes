package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import logica.Detallepedido;
import logica.Usuario;
import persistencias.JpaProvider;

public class SvDetallesPedido extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            // Solo admin puede ver detalles de cualquier pedido
            javax.servlet.http.HttpSession sess = request.getSession(false);
            Usuario usuario = (sess != null) ? (Usuario) sess.getAttribute("usuario") : null;
            if (usuario == null || !Boolean.TRUE.equals(sess.getAttribute("esAdmin"))) {
                out.print("{\"error\":\"No autorizado\"}");
                return;
            }

            String idStr = request.getParameter("idPedido");
            if (idStr == null || idStr.isBlank()) {
                out.print("{\"error\":\"idPedido requerido\"}");
                return;
            }

            int idPedido = Integer.parseInt(idStr.trim());

            EntityManager em = JpaProvider.getEntityManagerFactory().createEntityManager();
            try {
                TypedQuery<Detallepedido> q = em.createQuery(
                    "SELECT d FROM Detallepedido d WHERE d.pedido.idPedido = :pid", Detallepedido.class);
                q.setParameter("pid", idPedido);
                List<Detallepedido> detalles = q.getResultList();

                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < detalles.size(); i++) {
                    Detallepedido d = detalles.get(i);
                    if (i > 0) sb.append(",");
                    sb.append("{");
                    sb.append("\"producto\":\"").append(esc(d.getProducto() != null ? d.getProducto().getNombreProducto() : "Producto")).append("\",");
                    sb.append("\"cantidad\":").append(d.getCantidad()).append(",");
                    sb.append("\"precioUnitario\":").append(d.getPrecioUnitario() != null ? d.getPrecioUnitario() : 0).append(",");
                    sb.append("\"subtotal\":").append(d.getSubtotal());
                    sb.append("}");
                }
                sb.append("]");
                out.print(sb.toString());
            } finally {
                if (em.isOpen()) em.close();
            }

        } catch (NumberFormatException e) {
            out.print("{\"error\":\"idPedido inválido\"}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"" + esc(e.getMessage()) + "\"}");
        }
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", " ").replace("\r", "");
    }
}
