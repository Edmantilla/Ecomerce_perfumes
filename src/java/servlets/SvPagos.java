package servlets;                                        // declara que esta clase pertenece al paquete "servlets"

import enums.EstadoPago;                                 // enum con los estados de un pago: APROBADO, PENDIENTE, RECHAZADO
import java.io.IOException;                              // excepción de entrada/salida
import java.io.PrintWriter;                              // escribe texto en la respuesta HTTP
import java.math.BigDecimal;                             // tipo exacto para cálculos monetarios (suma de pagos, diferencias)
import java.sql.Timestamp;                               // convierte LocalDateTime al tipo compatible con JDBC para los UPDATE nativos
import java.time.LocalDateTime;                          // fecha+hora actual para el registro del pago
import java.util.List;                                   // lista de pagos
import javax.persistence.EntityManager;                  // gestiona la sesión activa con la BD
import javax.persistence.Query;                          // consulta JPA nativa; reemplaza TypedQuery en este servlet
import javax.servlet.ServletException;                   // excepción propia de los servlets
import javax.servlet.http.HttpServlet;                   // clase base de todos los servlets HTTP
import javax.servlet.http.HttpServletRequest;            // representa la petición HTTP entrante
import javax.servlet.http.HttpServletResponse;           // representa la respuesta HTTP saliente
import logica.Pago;                                      // entidad que representa un pago registrado
import logica.Pedido;                                    // entidad del pedido al que pertenece el pago
import persistencias.JpaProvider;                        // Singleton que provee la EntityManagerFactory
import persistencias.PagoJpaController;                  // controlador para CRUD en la tabla pago
import persistencias.PedidoJpaController;                // controlador para consultar el total del pedido

/**
 * SvPagos — Servlet de gestión de pagos de pedidos.
 * GET ?idPedido=X: retorna todos los pagos de un pedido, el total pagado y el monto pendiente.
 * POST: registra un nuevo pago. Si la suma acumulada cubre el total del pedido,
 *       cambia automáticamente el estado del pedido a PAGO.
 * POST accion=actualizar: modifica un pago existente (monto, método, estado).
 * Requiere permiso GESTIONAR_PAGOS o sesión activa.
 *
 * TODAS las consultas a BD usan createNativeQuery con SQL puro y parámetros posicionales (?).
 * EclipseLink no soporta parámetros nombrados (:x) en native queries → siempre usamos ?.
 */
@javax.servlet.annotation.WebServlet(name = "SvPagos", urlPatterns = {"/SvPagos"}) // cuando llegue a /SvPagos, Tomcat ejecuta esta clase
public class SvPagos extends HttpServlet {               // extiende HttpServlet para manejar peticiones HTTP

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) // se ejecuta cuando admin.js hace GET a /SvPagos?idPedido=X
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8"); // respuesta JSON en UTF-8
        PrintWriter out = response.getWriter();          // escritor de la respuesta

        // Verificar que el usuario tenga permiso para gestionar pagos O que esté logueado (clientes pueden ver sus pagos)
        if (!AuthHelper.tienePermiso(request, "GESTIONAR_PAGOS") && !AuthHelper.estaLogueado(request)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN); // HTTP 403
            out.print("{\"error\":\"Sin permiso: GESTIONAR_PAGOS\"}");
            return;
        }

        String idPedidoStr = request.getParameter("idPedido"); // ID del pedido del que se quieren ver los pagos
        EntityManager em = null;
        try {
            em = JpaProvider.getEntityManagerFactory().createEntityManager(); // abre sesión con la BD

            if (idPedidoStr != null) {                   // si se proporcionó un ID de pedido
                int idPedido = Integer.parseInt(idPedidoStr); // parsear el ID como entero

                // Consulta nativa para traer todos los pagos registrados de este pedido.
                // Reemplaza el JPQL: "SELECT p FROM Pago p WHERE p.pedido.idPedido = :id"
                // Traduce a SQL directo sobre la tabla pago filtrando por la FK id_pedido.
                // Pago.class instruye a EclipseLink para mapear cada fila al objeto Pago automáticamente.
                Query qPagos = em.createNativeQuery(
                    "SELECT * FROM pago WHERE id_pedido = ?", Pago.class);
                qPagos.setParameter(1, idPedido);        // ? pos.1 = id del pedido cuyos pagos se quieren consultar
                List<Pago> pagos = qPagos.getResultList(); // ejecuta SELECT y retorna lista de entidades Pago mapeadas

                // PedidoJpaController.findPedido() ya usa createNativeQuery internamente:
                // ejecuta: SELECT * FROM pedido WHERE id_pedido = ?
                Pedido pedido = new PedidoJpaController().findPedido(idPedido); // busca el pedido por su id para obtener el total
                BigDecimal totalPedido = pedido != null && pedido.getTotal() != null ? pedido.getTotal() : BigDecimal.ZERO; // total del pedido en BD

                // Suma los montos pagados en Java (sin JPQL); más claro y sin riesgo de nulls
                BigDecimal sumaPagada = pagos.stream()
                    .filter(p -> p.getMontoPagado() != null) // solo pagos con monto registrado
                    .map(Pago::getMontoPagado)           // extraer el monto de cada pago
                    .reduce(BigDecimal.ZERO, BigDecimal::add); // sumar todos los montos

                StringBuilder sb = new StringBuilder("{"); // inicia el objeto JSON de respuesta
                sb.append("\"totalPedido\":").append(totalPedido).append(","); // cuánto debe el pedido en total
                sb.append("\"sumaPagada\":").append(sumaPagada).append(",");   // cuánto se ha pagado hasta ahora
                sb.append("\"pendiente\":").append(totalPedido.subtract(sumaPagada)).append(","); // lo que falta por pagar
                sb.append("\"pagos\":[");                // inicia el array de pagos registrados
                for (int i = 0; i < pagos.size(); i++) { // iterar sobre cada pago
                    Pago p = pagos.get(i);               // pago actual
                    if (i > 0) sb.append(",");
                    sb.append("{");
                    sb.append("\"id\":").append(p.getIdPago()).append(","); // ID del pago
                    sb.append("\"metodo\":\"").append(escapeJson(p.getMetodoPago())).append("\","); // ej: "EFECTIVO", "TRANSFERENCIA"
                    sb.append("\"monto\":").append(p.getMontoPagado() != null ? p.getMontoPagado() : 0).append(","); // monto pagado
                    sb.append("\"estado\":\"").append(p.getEstadoPago() != null ? p.getEstadoPago().name() : "").append("\","); // ej: "APROBADO"
                    sb.append("\"fecha\":\"").append(p.getFechaPago() != null ? p.getFechaPago().toString() : "").append("\","); // fecha del pago
                    sb.append("\"referencia\":\"").append(escapeJson(p.getReferenciaTransaccion())).append("\""); // número de comprobante
                    sb.append("}");
                }
                sb.append("]}");                         // cierra el array de pagos y el objeto raíz
                out.print(sb.toString());                // envía el JSON al navegador
            } else {
                out.print("{\"error\":\"Se requiere idPedido\"}"); // no se proporcionó el ID del pedido
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        } finally {
            if (em != null && em.isOpen()) em.close();   // SIEMPRE cierra el EntityManager
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) // se ejecuta cuando admin.js hace POST a /SvPagos
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");           // fuerza UTF-8 para leer caracteres especiales
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        EntityManager em = null;
        try {
            // Verificar que haya sesión activa (admin o cliente que hace el pago)
            if (!AuthHelper.estaLogueado(request)) {     // no hay sesión activa
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // HTTP 401
                out.print("{\"error\":\"Debes iniciar sesi\u00f3n\"}"); // \u00f3 = ó (unicode)
                return;
            }

            String accion      = request.getParameter("accion");    // null = crear nuevo pago, "actualizar" = editar uno existente
            String idPedidoStr = request.getParameter("idPedido");  // ID del pedido al que pertenece el pago
            String metodo      = request.getParameter("metodo");    // método de pago: "EFECTIVO", "TRANSFERENCIA", "TARJETA"
            String montoStr    = request.getParameter("monto");     // monto del pago como texto
            String referencia  = request.getParameter("referencia"); // número de transacción o comprobante bancario

            // Validar que los campos obligatorios estén presentes
            if (idPedidoStr == null || montoStr == null || metodo == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // HTTP 400
                out.print("{\"error\":\"idPedido, metodo y monto son obligatorios\"}");
                return;
            }

            int idPedido = Integer.parseInt(idPedidoStr); // parsear el ID del pedido
            BigDecimal monto = new BigDecimal(montoStr); // convertir el monto a BigDecimal exacto

            if (monto.compareTo(BigDecimal.ZERO) <= 0) { // el monto debe ser positivo
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"El monto debe ser mayor a 0\"}");
                return;
            }

            // PedidoJpaController.findPedido() usa internamente createNativeQuery:
            // SELECT * FROM pedido WHERE id_pedido = ?
            PedidoJpaController pedidoCtrl = new PedidoJpaController();
            Pedido pedido = pedidoCtrl.findPedido(idPedido); // busca el pedido; retorna null si no existe
            if (pedido == null) {                        // si el pedido no existe en BD
                response.setStatus(HttpServletResponse.SC_NOT_FOUND); // HTTP 404
                out.print("{\"error\":\"Pedido no encontrado\"}");
                return;
            }

            em = JpaProvider.getEntityManagerFactory().createEntityManager(); // abre sesión con la BD

            // ACTUALIZAR pago existente: modifica monto, método, referencia o estado
            if ("actualizar".equals(accion)) {
                String idPagoStr = request.getParameter("idPago"); // ID del pago a modificar
                String estadoStr = request.getParameter("estado"); // nuevo estado del pago (ej: "RECHAZADO")
                if (idPagoStr == null) { out.print("{\"error\":\"idPago requerido\"}"); return; }
                PagoJpaController ctrl = new PagoJpaController(); // controlador para UPDATE en la tabla pago
                Pago pago = ctrl.findPago(Integer.parseInt(idPagoStr)); // SELECT * FROM pago WHERE id_pago = ?
                if (pago == null) { out.print("{\"error\":\"Pago no encontrado\"}"); return; }
                pago.setMetodoPago(metodo);              // actualiza el método de pago
                pago.setMontoPagado(monto);              // actualiza el monto
                if (referencia != null) pago.setReferenciaTransaccion(referencia); // actualiza la referencia si se envió
                if (estadoStr != null) pago.setEstadoPago(EstadoPago.valueOf(estadoStr)); // convierte texto a enum EstadoPago
                ctrl.edit(pago);                         // UPDATE pago SET metodo_pago=?, monto_pagado=?, ... WHERE id_pago=?
                out.print("{\"ok\":true}");
                return;
            }

            // NUEVO PAGO: calcular suma de pagos aprobados para verificar que no se exceda el total.
            // Reemplaza el JPQL: "SELECT COALESCE(SUM(p.montoPagado),0) FROM Pago p WHERE p.pedido.idPedido = :id AND p.estadoPago = :estado"
            // Traduce a SQL directo sobre la tabla pago filtrando por id_pedido y estado_pago.
            // COALESCE(SUM(monto_pagado), 0): si no hay pagos, SUM retorna NULL; COALESCE lo convierte a 0.
            // El resultado puede venir como BigDecimal o Integer según el driver; toString() + new BigDecimal() garantiza la conversión.
            Query qSum = em.createNativeQuery(
                "SELECT COALESCE(SUM(monto_pagado), 0) FROM pago WHERE id_pedido = ? AND estado_pago = ?");
            qSum.setParameter(1, idPedido);                  // ? pos.1 = id del pedido
            qSum.setParameter(2, EstadoPago.APROBADO.name()); // ? pos.2 = "APROBADO" (solo suma pagos aprobados)
            // new BigDecimal(toString()) es el cast más seguro: convierte cualquier tipo numérico retornado por MySQL
            BigDecimal sumaPagada = new BigDecimal(qSum.getSingleResult().toString());

            BigDecimal totalPedido = pedido.getTotal() != null ? pedido.getTotal() : BigDecimal.ZERO; // total del pedido
            BigDecimal pendiente = totalPedido.subtract(sumaPagada); // saldo pendiente = total - ya pagado

            // Rechazar si el monto del nuevo pago supera el saldo pendiente
            if (monto.compareTo(pendiente) > 0) {        // monto > pendiente: pago excesivo
                response.setStatus(HttpServletResponse.SC_CONFLICT); // HTTP 409
                out.print("{\"error\":\"El monto (" + monto + ") supera el pendiente (" + pendiente + ")\"}");
                return;
            }

            // Crear el nuevo pago y guardarlo en BD
            Pago pago = new Pago();                      // nuevo registro de pago
            pago.setPedido(pedido);                      // FK id_pedido → vincula el pago con el pedido
            pago.setMetodoPago(metodo);                  // método de pago (EFECTIVO, TARJETA, etc.)
            pago.setMontoPagado(monto);                  // monto de este pago
            pago.setFechaPago(LocalDateTime.now());      // fecha y hora en que se registra el pago
            pago.setEstadoPago(EstadoPago.APROBADO);     // se registra directamente como aprobado
            pago.setReferenciaTransaccion(referencia != null ? referencia : ""); // comprobante o cadena vacía
            pago.setActivo(true);                        // pago activo

            new PagoJpaController().create(pago);        // INSERT INTO pago (id_pedido, metodo_pago, monto_pagado, ...) VALUES (...)

            // Verificar si con este nuevo pago ya se cubre el total del pedido
            BigDecimal nuevaSuma = sumaPagada.add(monto); // total pagado después de este nuevo pago
            if (nuevaSuma.compareTo(totalPedido) >= 0) { // si ya se cubrió el total (o se superó)
                // Cambiar el estado del pedido a PAGO con una consulta nativa UPDATE.
                // Reemplaza: em.find(Pedido.class, idPedido) + setEstado() + em.merge(pedido)
                // Ahora es un único UPDATE directo: más eficiente (no carga el objeto Pedido en memoria).
                em.getTransaction().begin();             // inicia transacción para el UPDATE del estado del pedido
                Query qUpdate = em.createNativeQuery(
                    "UPDATE pedido SET estado = ?, updated_at = ? WHERE id_pedido = ?");
                qUpdate.setParameter(1, enums.EstadoPedido.PAGO.name());      // ? pos.1 → estado = "PAGO"
                qUpdate.setParameter(2, Timestamp.valueOf(LocalDateTime.now())); // ? pos.2 → updated_at = ahora
                qUpdate.setParameter(3, idPedido);                             // ? pos.3 → WHERE id_pedido = X
                qUpdate.executeUpdate();                 // ejecuta el UPDATE; cambia el estado del pedido a PAGO
                em.getTransaction().commit();            // confirma el cambio de estado en la base de datos
                out.print("{\"ok\":true,\"estadoActualizado\":\"PAGO\"}"); // notificar al frontend que el pedido pasó a PAGO
            } else {
                out.print("{\"ok\":true}"); // pago registrado pero el pedido aún no está completamente pagado
            }

        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback(); // revertir si hubo error
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        } finally {
            if (em != null && em.isOpen()) em.close();   // SIEMPRE cierra el EntityManager
        }
    }

    // Método auxiliar que escapa caracteres especiales para JSON
    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }
}
