package servlets;

import enums.EstadoPago;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import logica.Pago;
import logica.Pedido;
import persistencias.JpaProvider;
import persistencias.PagoJpaController;
import persistencias.PedidoJpaController;

@javax.servlet.annotation.WebServlet(name = "SvPagos", urlPatterns = {"/SvPagos"})
public class SvPagos extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String idPedidoStr = request.getParameter("idPedido");
        EntityManager em = null;
        try {
            em = JpaProvider.getEntityManagerFactory().createEntityManager();

            if (idPedidoStr != null) {
                int idPedido = Integer.parseInt(idPedidoStr);
                TypedQuery<Pago> q = em.createQuery(
                    "SELECT p FROM Pago p WHERE p.pedido.idPedido = :id", Pago.class);
                q.setParameter("id", idPedido);
                List<Pago> pagos = q.getResultList();

                Pedido pedido = new PedidoJpaController().findPedido(idPedido);
                BigDecimal totalPedido = pedido != null && pedido.getTotal() != null ? pedido.getTotal() : BigDecimal.ZERO;
                BigDecimal sumaPagada = pagos.stream()
                    .filter(p -> p.getMontoPagado() != null)
                    .map(Pago::getMontoPagado)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                StringBuilder sb = new StringBuilder("{");
                sb.append("\"totalPedido\":").append(totalPedido).append(",");
                sb.append("\"sumaPagada\":").append(sumaPagada).append(",");
                sb.append("\"pendiente\":").append(totalPedido.subtract(sumaPagada)).append(",");
                sb.append("\"pagos\":[");
                for (int i = 0; i < pagos.size(); i++) {
                    Pago p = pagos.get(i);
                    if (i > 0) sb.append(",");
                    sb.append("{");
                    sb.append("\"id\":").append(p.getIdPago()).append(",");
                    sb.append("\"metodo\":\"").append(escapeJson(p.getMetodoPago())).append("\",");
                    sb.append("\"monto\":").append(p.getMontoPagado() != null ? p.getMontoPagado() : 0).append(",");
                    sb.append("\"estado\":\"").append(p.getEstadoPago() != null ? p.getEstadoPago().name() : "").append("\",");
                    sb.append("\"fecha\":\"").append(p.getFechaPago() != null ? p.getFechaPago().toString() : "").append("\",");
                    sb.append("\"referencia\":\"").append(escapeJson(p.getReferenciaTransaccion())).append("\"");
                    sb.append("}");
                }
                sb.append("]}");
                out.print(sb.toString());
            } else {
                out.print("{\"error\":\"Se requiere idPedido\"}");
            }

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

        EntityManager em = null;
        try {
            String accion      = request.getParameter("accion");
            String idPedidoStr = request.getParameter("idPedido");
            String metodo      = request.getParameter("metodo");
            String montoStr    = request.getParameter("monto");
            String referencia  = request.getParameter("referencia");

            if (idPedidoStr == null || montoStr == null || metodo == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"idPedido, metodo y monto son obligatorios\"}");
                return;
            }

            int idPedido = Integer.parseInt(idPedidoStr);
            BigDecimal monto = new BigDecimal(montoStr);

            if (monto.compareTo(BigDecimal.ZERO) <= 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"El monto debe ser mayor a 0\"}");
                return;
            }

            PedidoJpaController pedidoCtrl = new PedidoJpaController();
            Pedido pedido = pedidoCtrl.findPedido(idPedido);
            if (pedido == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"Pedido no encontrado\"}");
                return;
            }

            em = JpaProvider.getEntityManagerFactory().createEntityManager();

            if ("actualizar".equals(accion)) {
                String idPagoStr = request.getParameter("idPago");
                String estadoStr = request.getParameter("estado");
                if (idPagoStr == null) { out.print("{\"error\":\"idPago requerido\"}"); return; }
                PagoJpaController ctrl = new PagoJpaController();
                Pago pago = ctrl.findPago(Integer.parseInt(idPagoStr));
                if (pago == null) { out.print("{\"error\":\"Pago no encontrado\"}"); return; }
                pago.setMetodoPago(metodo);
                pago.setMontoPagado(monto);
                if (referencia != null) pago.setReferenciaTransaccion(referencia);
                if (estadoStr != null) pago.setEstadoPago(EstadoPago.valueOf(estadoStr));
                ctrl.edit(pago);
                out.print("{\"ok\":true}");
                return;
            }

            // Validar que monto no exceda el pendiente (RF022)
            TypedQuery<BigDecimal> qSum = em.createQuery(
                "SELECT COALESCE(SUM(p.montoPagado),0) FROM Pago p WHERE p.pedido.idPedido = :id AND p.estadoPago = :estado",
                BigDecimal.class);
            qSum.setParameter("id", idPedido);
            qSum.setParameter("estado", EstadoPago.APROBADO);
            BigDecimal sumaPagada = qSum.getSingleResult();
            BigDecimal totalPedido = pedido.getTotal() != null ? pedido.getTotal() : BigDecimal.ZERO;
            BigDecimal pendiente = totalPedido.subtract(sumaPagada);

            if (monto.compareTo(pendiente) > 0) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                out.print("{\"error\":\"El monto (" + monto + ") supera el pendiente (" + pendiente + ")\"}");
                return;
            }

            Pago pago = new Pago();
            pago.setPedido(pedido);
            pago.setMetodoPago(metodo);
            pago.setMontoPagado(monto);
            pago.setFechaPago(LocalDateTime.now());
            pago.setEstadoPago(EstadoPago.APROBADO);
            pago.setReferenciaTransaccion(referencia != null ? referencia : "");
            pago.setActivo(true);

            new PagoJpaController().create(pago);

            // Si suma total == total pedido, actualizar estado a PAGO (RF022)
            BigDecimal nuevaSuma = sumaPagada.add(monto);
            if (nuevaSuma.compareTo(totalPedido) >= 0) {
                em.getTransaction().begin();
                Pedido pedidoMerge = em.find(Pedido.class, idPedido);
                if (pedidoMerge != null) {
                    pedidoMerge.setEstado(enums.EstadoPedido.PAGO);
                    pedidoMerge.setUpdatedAt(LocalDateTime.now());
                }
                em.getTransaction().commit();
                out.print("{\"ok\":true,\"estadoActualizado\":\"PAGO\"}");
            } else {
                out.print("{\"ok\":true}");
            }

        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
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
