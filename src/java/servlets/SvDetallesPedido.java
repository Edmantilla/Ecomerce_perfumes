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
import logica.Detallepedido;
import logica.Envio;
import persistencias.JpaProvider;

/**
 * SvDetallesPedido — Servlet que devuelve el detalle completo de un pedido específico.
 * GET ?idPedido=X: retorna un objeto JSON con:
 *   - items: lista de productos comprados (nombre, cantidad, precio unitario, subtotal)
 *   - envio: información del envío si existe (transportadora, guía, fechas, estado)
 * Es usado por el modal de detalle de pedido en el panel admin.
 * Requiere el permiso VER_PEDIDOS.
 */
@WebServlet(name = "SvDetallesPedido", urlPatterns = {"/SvDetallesPedido"})
public class SvDetallesPedido extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            if (!AuthHelper.tienePermiso(request, "VER_PEDIDOS")) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print("{\"error\":\"Sin permiso: VER_PEDIDOS\"}");
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
                // Consultar todos los productos (Detallepedido) de este pedido
                TypedQuery<Detallepedido> q = em.createQuery(
                    "SELECT d FROM Detallepedido d WHERE d.pedido.idPedido = :pid", Detallepedido.class);
                q.setParameter("pid", idPedido);
                List<Detallepedido> detalles = q.getResultList();

                // Construir el array JSON de items (productos del pedido)
                StringBuilder items = new StringBuilder("[");
                for (int i = 0; i < detalles.size(); i++) {
                    Detallepedido d = detalles.get(i);
                    if (i > 0) items.append(",");
                    items.append("{");
                    items.append("\"producto\":\"").append(esc(d.getProducto() != null ? d.getProducto().getNombreProducto() : "Producto")).append("\",");
                    items.append("\"cantidad\":").append(d.getCantidad()).append(",");
                    items.append("\"precioUnitario\":").append(d.getPrecioUnitario() != null ? d.getPrecioUnitario() : 0).append(",");
                    items.append("\"subtotal\":").append(d.getSubtotal()); // precio * cantidad
                    items.append("}");
                }
                items.append("]");

                // Consultar el envío asociado al pedido (puede no existir si aún no se ha despachado)
                TypedQuery<Envio> qe = em.createQuery(
                    "SELECT e FROM Envio e WHERE e.pedido.idPedido = :pid", Envio.class);
                qe.setParameter("pid", idPedido);
                List<Envio> envios = qe.getResultList();

                // Si no hay envío, el campo envio será null en el JSON
                StringBuilder envioJson = new StringBuilder("null");
                if (!envios.isEmpty()) {
                    Envio e = envios.get(0);
                    envioJson = new StringBuilder("{");
                    envioJson.append("\"transportadora\":\"").append(esc(e.getTransportadora())).append("\",");
                    envioJson.append("\"guia\":\"").append(esc(e.getNumeroGuia())).append("\",");
                    envioJson.append("\"estado\":\"").append(e.getEstadoEntrega() != null ? e.getEstadoEntrega().name() : "").append("\",");
                    // Solo la parte de fecha (sin hora) para mostrar en el modal del admin
                    envioJson.append("\"fechaEnvio\":\"").append(e.getFechaEnvio() != null ? e.getFechaEnvio().toLocalDate().toString() : "").append("\",");
                    envioJson.append("\"fechaEstimada\":\"").append(e.getFechaEstimadaEntrega() != null ? e.getFechaEstimadaEntrega().toLocalDate().toString() : "").append("\"");
                    envioJson.append("}");
                }

                // Retornar el objeto completo: items + envio
                out.print("{\"items\":" + items.toString() + ",\"envio\":" + envioJson.toString() + "}");

            } finally {
                if (em.isOpen()) em.close();
            }

        } catch (NumberFormatException e) {
            out.print("{\"error\":\"idPedido inv\u00e1lido\"}");
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
