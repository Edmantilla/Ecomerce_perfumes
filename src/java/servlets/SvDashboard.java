package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import logica.Pedido;
import persistencias.JpaProvider;
import persistencias.PedidoJpaController;
import persistencias.ProductoJpaController;
import persistencias.UsuarioJpaController;

/**
 * SvDashboard — Servlet que provee datos para el panel principal del administrador.
 * GET: retorna un JSON con las métricas generales del sistema:
 *   - ventas: suma de los totales de todos los pedidos excepto los cancelados
 *   - productos: cantidad total de productos
 *   - pedidos: cantidad total de pedidos
 *   - usuarios: cantidad total de usuarios
 *   - pedidosRecientes: los 5 pedidos más recientes con cliente, total y estado
 * Requiere el permiso VER_DASHBOARD.
 */
@WebServlet(name = "SvDashboard", urlPatterns = {"/SvDashboard"})
public class SvDashboard extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        if (!AuthHelper.tienePermiso(request, "VER_DASHBOARD")) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            out.print("{\"error\":\"Sin permiso: VER_DASHBOARD\"}");
            return;
        }

        EntityManager em = null;
        try {
            em = JpaProvider.getEntityManagerFactory().createEntityManager();

            // Obtener contadores rápidos usando los métodos count() de cada controlador JPA
            long totalProductos = new ProductoJpaController().getProductoCount();
            long totalUsuarios  = new UsuarioJpaController().getUsuarioCount();
            long totalPedidos   = new PedidoJpaController().getPedidoCount();

            // Sumar los totales de todos los pedidos EXCEPTO los cancelados
            // COALESCE convierte null a 0 si no hay pedidos
            TypedQuery<BigDecimal> qVentas = em.createQuery(
                "SELECT COALESCE(SUM(p.total), 0) FROM Pedido p WHERE p.estado <> enums.EstadoPedido.CANCELADO", BigDecimal.class
            );
            BigDecimal ventas = qVentas.getSingleResult();

            // Traer los 5 pedidos más recientes para mostrar en la tabla del dashboard
            // COALESCE en la fecha permite usar createdAt si fechaPedido es null
            TypedQuery<Pedido> qRecientes = em.createQuery(
                "SELECT p FROM Pedido p ORDER BY COALESCE(p.fechaPedido, p.createdAt) DESC", Pedido.class
            );
            qRecientes.setMaxResults(5); // solo los 5 más recientes
            List<Pedido> recientes = qRecientes.getResultList();

            // Construir el objeto JSON de respuesta para el dashboard
            StringBuilder sb = new StringBuilder("{");
            sb.append("\"ventas\":").append(ventas != null ? ventas : BigDecimal.ZERO).append(",");
            sb.append("\"productos\":").append(totalProductos).append(",");
            sb.append("\"pedidos\":").append(totalPedidos).append(",");
            sb.append("\"usuarios\":").append(totalUsuarios).append(",");
            sb.append("\"pedidosRecientes\":[");
            for (int i = 0; i < recientes.size(); i++) {
                Pedido p = recientes.get(i);
                if (i > 0) sb.append(",");
                sb.append("{");
                sb.append("\"id\":").append(p.getIdPedido()).append(",");
                sb.append("\"cliente\":\"").append(
                    p.getCliente() != null ? escapeJson(p.getCliente().getNombreCompleto()) : ""
                ).append("\",");
                sb.append("\"total\":").append(p.getTotal() != null ? p.getTotal() : 0).append(",");
                // Usar fechaPedido si está disponible, si no usar createdAt como respaldo
                sb.append("\"fecha\":\"").append(
                    p.getFechaPedido() != null ? p.getFechaPedido().toString() :
                    (p.getCreatedAt() != null ? p.getCreatedAt().toString() : "")
                ).append("\",");
                sb.append("\"estado\":\"").append(p.getEstado() != null ? p.getEstado().name() : "").append("\"");
                sb.append("}");
            }
            sb.append("]}");
            out.print(sb.toString());

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        } finally {
            if (em != null && em.isOpen()) em.close();
        }
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }
}
