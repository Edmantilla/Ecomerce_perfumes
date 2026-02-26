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
import logica.Pedido;
import logica.Usuario;
import persistencias.JpaProvider;

public class SvMisPedidos extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            javax.servlet.http.HttpSession sess = request.getSession(false);
            Usuario usuario = (sess != null) ? (Usuario) sess.getAttribute("usuario") : null;

            if (usuario == null) {
                out.print("{\"error\":\"no-session\"}");
                return;
            }

            EntityManager em = JpaProvider.getEntityManagerFactory().createEntityManager();
            try {
                TypedQuery<Pedido> q = em.createQuery(
                    "SELECT p FROM Pedido p WHERE p.cliente.idCliente = :cid ORDER BY p.fechaPedido DESC",
                    Pedido.class);
                q.setParameter("cid", usuario.getCliente() != null
                    ? usuario.getCliente().getIdCliente()
                    : em.find(Usuario.class, usuario.getIdUsuario()).getCliente().getIdCliente());
                List<Pedido> pedidos = q.getResultList();

                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < pedidos.size(); i++) {
                    Pedido p = pedidos.get(i);
                    if (i > 0) sb.append(",");
                    sb.append("{");
                    sb.append("\"id\":").append(p.getIdPedido()).append(",");
                    sb.append("\"estado\":\"").append(p.getEstado() != null ? p.getEstado().name() : "PENDIENTE").append("\",");
                    sb.append("\"fecha\":\"").append(p.getFechaPedido() != null ? p.getFechaPedido().toString().replace("T", " ").substring(0, 16) : "").append("\",");
                    sb.append("\"total\":").append(p.getTotal() != null ? p.getTotal() : 0).append(",");

                    // Detalles del pedido
                    sb.append("\"detalles\":[");
                    TypedQuery<Detallepedido> dq = em.createQuery(
                        "SELECT d FROM Detallepedido d WHERE d.pedido.idPedido = :pid", Detallepedido.class);
                    dq.setParameter("pid", p.getIdPedido());
                    List<Detallepedido> detalles = dq.getResultList();
                    for (int j = 0; j < detalles.size(); j++) {
                        Detallepedido d = detalles.get(j);
                        if (j > 0) sb.append(",");
                        sb.append("{");
                        sb.append("\"producto\":\"").append(esc(d.getProducto() != null ? d.getProducto().getNombreProducto() : "Producto")).append("\",");
                        sb.append("\"cantidad\":").append(d.getCantidad()).append(",");
                        sb.append("\"precioUnitario\":").append(d.getPrecioUnitario() != null ? d.getPrecioUnitario() : 0).append(",");
                        sb.append("\"subtotal\":").append(d.getSubtotal());
                        sb.append("}");
                    }
                    sb.append("]");
                    sb.append("}");
                }
                sb.append("]");
                out.print(sb.toString());
            } finally {
                if (em.isOpen()) em.close();
            }

        } catch (Exception e) {
            java.io.StringWriter sw = new java.io.StringWriter();
            e.printStackTrace(new java.io.PrintWriter(sw));
            out.print("{\"error\":\"" + esc(sw.toString().substring(0, Math.min(300, sw.toString().length()))) + "\"}");
        }
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", " ").replace("\r", "");
    }
}
