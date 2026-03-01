package servlets;

import enums.EstadoEntrega;
import enums.EstadoPedido;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import logica.Envio;
import logica.Pedido;
import persistencias.EnvioJpaController;
import persistencias.JpaProvider;
import persistencias.PedidoJpaController;

public class SvEnvios extends HttpServlet {

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
                TypedQuery<Envio> q = em.createQuery(
                    "SELECT e FROM Envio e WHERE e.pedido.idPedido = :id", Envio.class);
                q.setParameter("id", idPedido);
                List<Envio> envios = q.getResultList();

                if (envios.isEmpty()) {
                    out.print("{\"envio\":null}");
                } else {
                    Envio e = envios.get(0);
                    StringBuilder sb = new StringBuilder("{\"envio\":{");
                    sb.append("\"id\":").append(e.getIdEnvio()).append(",");
                    sb.append("\"direccion\":\"").append(escapeJson(e.getDireccionEnvio())).append("\",");
                    sb.append("\"transportadora\":\"").append(escapeJson(e.getTransportadora())).append("\",");
                    sb.append("\"guia\":\"").append(escapeJson(e.getNumeroGuia())).append("\",");
                    sb.append("\"estado\":\"").append(e.getEstadoEntrega() != null ? e.getEstadoEntrega().name() : "").append("\",");
                    sb.append("\"fechaEnvio\":\"").append(e.getFechaEnvio() != null ? e.getFechaEnvio().toString() : "").append("\",");
                    sb.append("\"fechaEstimada\":\"").append(e.getFechaEstimadaEntrega() != null ? e.getFechaEstimadaEntrega().toString() : "").append("\"");
                    sb.append("}}");
                    out.print(sb.toString());
                }
            } else {
                out.print("{\"error\":\"Se requiere idPedido\"}");
            }

        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"" + escapeJson(ex.getMessage()) + "\"}");
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
            String accion          = request.getParameter("accion");
            String idPedidoStr     = request.getParameter("idPedido");
            String direccion       = request.getParameter("direccion");
            String transportadora  = request.getParameter("transportadora");
            String guia            = request.getParameter("guia");
            String fechaEstStr     = request.getParameter("fechaEstimada");
            String estadoStr       = request.getParameter("estado");

            if (idPedidoStr == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"idPedido es obligatorio\"}");
                return;
            }

            int idPedido = Integer.parseInt(idPedidoStr);
            PedidoJpaController pedidoCtrl = new PedidoJpaController();
            Pedido pedido = pedidoCtrl.findPedido(idPedido);
            if (pedido == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"Pedido no encontrado\"}");
                return;
            }

            em = JpaProvider.getEntityManagerFactory().createEntityManager();
            EnvioJpaController envioCtrl = new EnvioJpaController();

            if ("actualizar".equals(accion)) {
                // Actualizar estado de entrega de un envío existente (RF025)
                String idEnvioStr = request.getParameter("idEnvio");
                if (idEnvioStr == null || estadoStr == null) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"idEnvio y estado son obligatorios\"}");
                    return;
                }
                Envio envio = envioCtrl.findEnvio(Integer.parseInt(idEnvioStr));
                if (envio == null) { out.print("{\"error\":\"Env\u00edo no encontrado\"}"); return; }
                EstadoEntrega nuevoEstado = EstadoEntrega.valueOf(estadoStr);
                envio.setEstadoEntrega(nuevoEstado);
                envio.setUpdatedAt(LocalDateTime.now());
                if (guia != null && !guia.isBlank()) envio.setNumeroGuia(guia);
                if (nuevoEstado == EstadoEntrega.ENTREGADO) {
                    em.getTransaction().begin();
                    Pedido p = em.find(Pedido.class, idPedido);
                    if (p != null) { p.setEstado(EstadoPedido.ENTREGADO); p.setUpdatedAt(LocalDateTime.now()); }
                    em.getTransaction().commit();
                }
                envioCtrl.edit(envio);
                out.print("{\"ok\":true}");
                return;
            }

            // Crear nuevo envío (RF023/RF024)
            // Verificar que no existe ya un envío para este pedido
            TypedQuery<Long> checkQ = em.createQuery(
                "SELECT COUNT(e) FROM Envio e WHERE e.pedido.idPedido = :id", Long.class);
            checkQ.setParameter("id", idPedido);
            if (checkQ.getSingleResult() > 0) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                out.print("{\"error\":\"Este pedido ya tiene un env\u00edo registrado\"}");
                return;
            }

            if (direccion == null || direccion.isBlank()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"La direcci\u00f3n de env\u00edo es obligatoria\"}");
                return;
            }

            Envio envio = new Envio();
            envio.setPedido(pedido);
            envio.setDireccionEnvio(direccion);
            envio.setTransportadora(transportadora != null ? transportadora : "");
            envio.setNumeroGuia(guia != null ? guia : "");
            envio.setFechaEnvio(LocalDateTime.now());
            envio.setEstadoEntrega(EstadoEntrega.PREPARANDO);
            envio.setCreatedAt(LocalDateTime.now());
            envio.setUpdatedAt(LocalDateTime.now());
            envio.setActivo(true);

            if (fechaEstStr != null && !fechaEstStr.isBlank()) {
                try {
                    envio.setFechaEstimadaEntrega(LocalDate.parse(fechaEstStr).atStartOfDay());
                } catch (Exception ignored) {}
            }

            envioCtrl.create(envio);

            // Cambiar estado del pedido a ENVIADO
            em.getTransaction().begin();
            Pedido pedidoMerge = em.find(Pedido.class, idPedido);
            if (pedidoMerge != null) {
                pedidoMerge.setEstado(EstadoPedido.ENVIADO);
                pedidoMerge.setUpdatedAt(LocalDateTime.now());
            }
            em.getTransaction().commit();

            out.print("{\"ok\":true}");

        } catch (Exception ex) {
            if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"" + escapeJson(ex.getMessage()) + "\"}");
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
