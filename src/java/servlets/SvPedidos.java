package servlets;

import enums.EstadoPedido;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import logica.Pedido;
import logica.Telefonocliente;
import logica.Usuario;
import persistencias.JpaProvider;
import persistencias.PedidoJpaController;

/**
 * SvPedidos — Servlet de gestión de pedidos para el panel admin.
 * GET: lista todos los pedidos con datos del cliente (requiere VER_PEDIDOS).
 * POST accion=cambiarEstado: actualiza el estado de un pedido (requiere GESTIONAR_PEDIDOS).
 */
@WebServlet(name = "SvPedidos", urlPatterns = {"/SvPedidos"})
public class SvPedidos extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        if (!AuthHelper.tienePermiso(request, "VER_PEDIDOS")) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            out.print("{\"error\":\"Sin permiso: VER_PEDIDOS\"}");
            return;
        }

        EntityManager emGet = null;
        try {
            PedidoJpaController ctrl = new PedidoJpaController();
            List<Pedido> pedidos = ctrl.findPedidoEntities(); // traer todos los pedidos de la BD
            // EntityManager separado para consultar teléfonos de cada cliente sin conflicto de transacción
            emGet = JpaProvider.getEntityManagerFactory().createEntityManager();

            // Construir el array JSON de pedidos
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < pedidos.size(); i++) {
                Pedido p = pedidos.get(i);
                if (i > 0) sb.append(",");
                sb.append("{");
                sb.append("\"id\":").append(p.getIdPedido()).append(",");
                sb.append("\"estado\":\"").append(esc(p.getEstado() != null ? p.getEstado().name() : "")).append("\",");
                sb.append("\"fecha\":\"").append(p.getFechaPedido() != null ? p.getFechaPedido().toString() : "").append("\",");
                sb.append("\"total\":").append(p.getTotal() != null ? p.getTotal() : 0).append(",");
                sb.append("\"cliente\":\"").append(
                    p.getCliente() != null ? esc(p.getCliente().getNombreCompleto()) : ""
                ).append("\",");
                if (p.getCliente() != null) {
                    int idC = p.getCliente().getIdCliente();
                    String dir = p.getCliente().getDireccion();
                    sb.append("\"direccionCliente\":\"").append(esc(dir != null ? dir : "")).append("\",");
                    // Consultar los teléfonos activos del cliente para mostrarlos en el panel
                    List<Telefonocliente> tels = emGet.createQuery(
                        "SELECT t FROM Telefonocliente t WHERE t.cliente.idCliente = :id AND t.activo = true ORDER BY t.idTelefono",
                        Telefonocliente.class).setParameter("id", idC).getResultList();
                    sb.append("\"telefonosCliente\":[");
                    for (int j = 0; j < tels.size(); j++) {
                        if (j > 0) sb.append(",");
                        sb.append("\"").append(esc(tels.get(j).getTelefono())).append("\"");
                    }
                    sb.append("]");
                } else {
                    // Pedido sin cliente (caso admin u otro)
                    sb.append("\"direccionCliente\":\"\",\"telefonosCliente\":[]" );
                }
                sb.append("}");
            }
            sb.append("]");
            out.print(sb.toString());

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"" + esc(e.getMessage()) + "\"}");
        } finally {
            if (emGet != null && emGet.isOpen()) emGet.close();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            if (!AuthHelper.tienePermiso(request, "GESTIONAR_PEDIDOS")) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print("{\"error\":\"Sin permiso: GESTIONAR_PEDIDOS\"}");
                return;
            }

            String accion = request.getParameter("accion");

            if ("cambiarEstado".equals(accion)) {
                String idStr     = request.getParameter("idPedido");
                String estadoStr = request.getParameter("estado"); // debe coincidir con un valor del enum EstadoPedido

                if (idStr == null || estadoStr == null) {
                    out.print("{\"error\":\"Parámetros faltantes\"}");
                    return;
                }

                int idPedido = Integer.parseInt(idStr.trim());
                // Convertir el texto recibido al enum EstadoPedido (lanza IllegalArgumentException si no coincide)
                EstadoPedido nuevoEstado = EstadoPedido.valueOf(estadoStr.trim().toUpperCase());

                EntityManager em = JpaProvider.getEntityManagerFactory().createEntityManager();
                try {
                    em.getTransaction().begin();
                    Pedido pedido = em.find(Pedido.class, idPedido); // buscar el pedido por ID
                    if (pedido == null) {
                        em.getTransaction().rollback();
                        out.print("{\"error\":\"Pedido no encontrado\"}");
                        return;
                    }
                    // Actualizar el estado y la fecha de modificación
                    pedido.setEstado(nuevoEstado);
                    pedido.setUpdatedAt(LocalDateTime.now());
                    em.merge(pedido); // UPDATE en BD
                    em.getTransaction().commit();
                    out.print("{\"ok\":true,\"idPedido\":" + idPedido + ",\"estado\":\"" + nuevoEstado.name() + "\"}");
                } finally {
                    if (em.isOpen()) em.close();
                }
            } else {
                out.print("{\"error\":\"Acción desconocida\"}");
            }

        } catch (IllegalArgumentException e) {
            out.print("{\"error\":\"Estado inválido: " + esc(e.getMessage()) + "\"}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"" + esc(e.getMessage()) + "\"}");
        }
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }
}
