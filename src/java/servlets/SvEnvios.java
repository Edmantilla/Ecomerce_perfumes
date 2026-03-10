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
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import logica.Envio;
import logica.Pedido;
import persistencias.EnvioJpaController;
import persistencias.JpaProvider;
import persistencias.PedidoJpaController;

/**
 * SvEnvios — Servlet de gestión de envíos de pedidos.
 * GET ?idPedido=X: retorna el envío asociado a un pedido (requiere sesión activa).
 * POST: crea un nuevo envío y cambia el estado del pedido a ENVIADO automáticamente.
 * POST accion=actualizar: cambia el estado del envío. Si pasa a ENTREGADO,
 *      cambia también el estado del pedido a ENTREGADO automáticamente.
 * Requiere permiso GESTIONAR_ENVIOS para el POST.
 */
@WebServlet(name = "SvEnvios", urlPatterns = {"/SvEnvios"})
public class SvEnvios extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        if (!AuthHelper.estaLogueado(request)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"error\":\"Debes iniciar sesi\u00f3n\"}");
            return;
        }

        String idPedidoStr = request.getParameter("idPedido");
        EntityManager em = null;
        try {
            em = JpaProvider.getEntityManagerFactory().createEntityManager();

            if (idPedidoStr != null) {
                int idPedido = Integer.parseInt(idPedidoStr);
                // Buscar el envío asociado al pedido dado su ID
                TypedQuery<Envio> q = em.createQuery(
                    "SELECT e FROM Envio e WHERE e.pedido.idPedido = :id", Envio.class);
                q.setParameter("id", idPedido);
                List<Envio> envios = q.getResultList();

                if (envios.isEmpty()) {
                    // El pedido todavía no tiene envío registrado
                    out.print("{\"envio\":null}");
                } else {
                    // Serializar los datos del envío a JSON
                    Envio e = envios.get(0);
                    StringBuilder sb = new StringBuilder("{\"envio\":{");
                    sb.append("\"id\":").append(e.getIdEnvio()).append(",");
                    sb.append("\"direccion\":\"").append(escapeJson(e.getDireccionEnvio())).append("\",");
                    sb.append("\"transportadora\":\"").append(escapeJson(e.getTransportadora())).append("\",");
                    sb.append("\"guia\":\"").append(escapeJson(e.getNumeroGuia())).append("\",");
                    sb.append("\"estado\":\"").append(e.getEstadoEntrega() != null ? e.getEstadoEntrega().name() : "").append("\",");
                    // Usar solo la parte de fecha (sin hora) para presentación en el frontend
                    sb.append("\"fechaEnvio\":\"").append(e.getFechaEnvio() != null ? e.getFechaEnvio().toLocalDate().toString() : "").append("\",");
                    sb.append("\"fechaEstimada\":\"").append(e.getFechaEstimadaEntrega() != null ? e.getFechaEstimadaEntrega().toLocalDate().toString() : "").append("\"");
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
            if (!AuthHelper.tienePermiso(request, "GESTIONAR_ENVIOS")) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print("{\"error\":\"Sin permiso: GESTIONAR_ENVIOS\"}");
                return;
            }

            String accion          = request.getParameter("accion");        // null = crear, "actualizar" = cambiar estado
            String idPedidoStr     = request.getParameter("idPedido");
            String direccion       = request.getParameter("direccion");      // dirección física de entrega
            String transportadora  = request.getParameter("transportadora"); // ej: "Servientrega", "Coordinadora"
            String guia            = request.getParameter("guia");           // número de guía de la transportadora
            String fechaEstStr     = request.getParameter("fechaEstimada");  // formato YYYY-MM-DD
            String estadoStr       = request.getParameter("estado");         // valor del enum EstadoEntrega

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

            // ACTUALIZAR: cambiar el estado del envío existente
            if ("actualizar".equals(accion)) {
                String idEnvioStr = request.getParameter("idEnvio");
                if (idEnvioStr == null || estadoStr == null) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"idEnvio y estado son obligatorios\"}");
                    return;
                }
                Envio envio = envioCtrl.findEnvio(Integer.parseInt(idEnvioStr));
                if (envio == null) { out.print("{\"error\":\"Envío no encontrado\"}"); return; }
                EstadoEntrega nuevoEstado = EstadoEntrega.valueOf(estadoStr); // convertir texto a enum
                envio.setEstadoEntrega(nuevoEstado);
                envio.setUpdatedAt(LocalDateTime.now());
                if (guia != null && !guia.isBlank()) envio.setNumeroGuia(guia); // actualizar guía si se envió
                // Si el envío pasa a ENTREGADO, actualizar también el estado del pedido
                if (nuevoEstado == EstadoEntrega.ENTREGADO) {
                    em.getTransaction().begin();
                    Pedido p = em.find(Pedido.class, idPedido);
                    if (p != null) { p.setEstado(EstadoPedido.ENTREGADO); p.setUpdatedAt(LocalDateTime.now()); }
                    em.getTransaction().commit();
                }
                envioCtrl.edit(envio); // UPDATE en BD
                out.print("{\"ok\":true}");
                return;
            }

            // CREAR: registrar un nuevo envío para el pedido
            // Verificar que el pedido no tenga ya un envío registrado (sólo puede tener uno)
            TypedQuery<Long> checkQ = em.createQuery(
                "SELECT COUNT(e) FROM Envio e WHERE e.pedido.idPedido = :id", Long.class);
            checkQ.setParameter("id", idPedido);
            if (checkQ.getSingleResult() > 0) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                out.print("{\"error\":\"Este pedido ya tiene un envío registrado\"}");
                return;
            }

            if (direccion == null || direccion.isBlank()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"La dirección de envío es obligatoria\"}");
                return;
            }

            // Construir el objeto Envio con los datos recibidos
            Envio envio = new Envio();
            envio.setPedido(pedido);
            envio.setDireccionEnvio(direccion);
            envio.setTransportadora(transportadora != null ? transportadora : "");
            envio.setNumeroGuia(guia != null ? guia : "");
            envio.setFechaEnvio(LocalDateTime.now()); // fecha actual como fecha de despacho
            envio.setEstadoEntrega(EstadoEntrega.PREPARANDO); // estado inicial del envío
            envio.setCreatedAt(LocalDateTime.now());
            envio.setUpdatedAt(LocalDateTime.now());
            envio.setActivo(true);

            // Parsear la fecha estimada de entrega si fue enviada (formato YYYY-MM-DD)
            if (fechaEstStr != null && !fechaEstStr.isBlank()) {
                try {
                    envio.setFechaEstimadaEntrega(LocalDate.parse(fechaEstStr).atStartOfDay());
                } catch (Exception ignored) {} // si el formato es inválido, ignorar
            }

            envioCtrl.create(envio); // INSERT en BD

            // Cambiar el estado del pedido a ENVIADO automáticamente al registrar el envío
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
