package servlets; // Este archivo pertenece al paquete "servlets"

import enums.EstadoEntrega; // Enum con los estados del envío: PREPARANDO, EN_CAMINO, ENTREGADO, etc.
import enums.EstadoPedido; // Enum con los estados del pedido: PENDIENTE, PAGO, ENVIADO, ENTREGADO, CANCELADO
import java.io.IOException; // Excepción para errores de entrada/salida
import java.io.PrintWriter; // Para escribir texto en la respuesta HTTP
import java.time.LocalDate; // Para manejar solo fechas (sin hora) al parsear la fecha estimada
import java.time.LocalDateTime; // Para manejar fechas y horas en los timestamps
import java.util.List; // Para manejar listas de entidades
import javax.persistence.EntityManager; // Maneja la conexión y operaciones con la BD
import javax.persistence.TypedQuery; // Para ejecutar consultas JPQL con tipo específico
import javax.servlet.ServletException; // Excepción propia de los servlets
import javax.servlet.annotation.WebServlet; // Anotación para registrar el servlet en una URL
import javax.servlet.http.HttpServlet; // Clase base de todos los servlets HTTP
import javax.servlet.http.HttpServletRequest; // Representa la petición que llega del cliente
import javax.servlet.http.HttpServletResponse; // Representa la respuesta que se envía al cliente
import logica.Envio; // Entidad JPA que representa un envío
import logica.Pedido; // Entidad JPA que representa un pedido
import persistencias.EnvioJpaController; // Controlador JPA con CRUD de envíos
import persistencias.JpaProvider; // Singleton que provee el EntityManagerFactory
import persistencias.PedidoJpaController; // Controlador JPA con CRUD de pedidos

/**
 * SvEnvios — Servlet de gestión de envíos de pedidos.
 * GET ?idPedido=X: retorna el envío asociado a un pedido (requiere sesión activa).
 * POST: crea un nuevo envío y cambia el estado del pedido a ENVIADO automáticamente.
 * POST accion=actualizar: cambia el estado del envío. Si pasa a ENTREGADO,
 *      cambia también el estado del pedido a ENTREGADO automáticamente.
 * Requiere permiso GESTIONAR_ENVIOS para el POST.
 */
@WebServlet(name = "SvEnvios", urlPatterns = {"/SvEnvios"}) // Registra este servlet en la URL /SvEnvios
public class SvEnvios extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8"); // La respuesta será JSON en UTF-8
        PrintWriter out = response.getWriter(); // Obtiene el escritor para enviar texto al cliente

        // Verificar que haya sesión activa (clientes y admins pueden consultar envíos)
        if (!AuthHelper.estaLogueado(request)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // Responde 401 Unauthorized
            out.print("{\"error\":\"Debes iniciar sesi\u00f3n\"}"); // \u00f3 = ó en unicode
            return; // Corta la ejecución
        }

        String idPedidoStr = request.getParameter("idPedido"); // ID del pedido cuyo envío se quiere consultar
        EntityManager em = null; // Se declara fuera del try para poder cerrarlo en el finally
        try {
            em = JpaProvider.getEntityManagerFactory().createEntityManager(); // Abre la conexión con la BD

            if (idPedidoStr != null) {
                int idPedido = Integer.parseInt(idPedidoStr); // Convierte el ID de String a entero
                // Buscar el envío asociado al pedido dado su ID
                TypedQuery<Envio> q = em.createQuery(
                    "SELECT e FROM Envio e WHERE e.pedido.idPedido = :id", Envio.class); // JPQL: busca envío por ID de pedido
                q.setParameter("id", idPedido);
                List<Envio> envios = q.getResultList(); // Ejecuta la consulta

                if (envios.isEmpty()) {
                    // El pedido todavía no tiene envío registrado
                    out.print("{\"envio\":null}"); // Responde null para que el frontend lo maneje
                } else {
                    // Serializar los datos del envío a JSON
                    Envio e = envios.get(0); // Toma el único envío (cada pedido tiene máximo uno)
                    StringBuilder sb = new StringBuilder("{\"envio\":{");
                    sb.append("\"id\":").append(e.getIdEnvio()).append(","); // ID del envío
                    sb.append("\"direccion\":\"").append(escapeJson(e.getDireccionEnvio())).append("\","); // Dirección física de entrega
                    sb.append("\"transportadora\":\"").append(escapeJson(e.getTransportadora())).append("\","); // Nombre de la transportadora
                    sb.append("\"guia\":\"").append(escapeJson(e.getNumeroGuia())).append("\","); // Número de guía de seguimiento
                    sb.append("\"estado\":\"").append(e.getEstadoEntrega() != null ? e.getEstadoEntrega().name() : "").append("\","); // Estado del envío como string del enum
                    // Usar solo la parte de fecha (sin hora) para presentación en el frontend
                    sb.append("\"fechaEnvio\":\"").append(e.getFechaEnvio() != null ? e.getFechaEnvio().toLocalDate().toString() : "").append("\","); // Fecha de despacho (YYYY-MM-DD)
                    sb.append("\"fechaEstimada\":\"").append(e.getFechaEstimadaEntrega() != null ? e.getFechaEstimadaEntrega().toLocalDate().toString() : "").append("\""); // Fecha estimada de entrega (YYYY-MM-DD)
                    sb.append("}}"); // Cierra el objeto envio y el objeto raíz
                    out.print(sb.toString()); // Envía el JSON al cliente
                }
            } else {
                out.print("{\"error\":\"Se requiere idPedido\"}"); // El parámetro idPedido es obligatorio
            }

        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // Responde 500 si ocurre cualquier error
            out.print("{\"error\":\"" + escapeJson(ex.getMessage()) + "\"}");
        } finally {
            if (em != null && em.isOpen()) em.close(); // Siempre cierra el EntityManager para evitar leaks de conexión
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8"); // Fuerza UTF-8 en los parámetros del request
        response.setContentType("application/json;charset=UTF-8"); // La respuesta será JSON en UTF-8
        PrintWriter out = response.getWriter(); // Obtiene el escritor para enviar texto al cliente

        EntityManager em = null; // Se declara fuera del try para poder cerrarlo en el finally
        try {
            // Verificar que el usuario tenga el permiso GESTIONAR_ENVIOS
            if (!AuthHelper.tienePermiso(request, "GESTIONAR_ENVIOS")) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN); // Responde 403 Forbidden
                out.print("{\"error\":\"Sin permiso: GESTIONAR_ENVIOS\"}");
                return; // Corta la ejecución
            }

            String accion          = request.getParameter("accion");        // null = crear envío, "actualizar" = cambiar estado
            String idPedidoStr     = request.getParameter("idPedido");      // ID del pedido al que pertenece el envío
            String direccion       = request.getParameter("direccion");      // Dirección física de entrega
            String transportadora  = request.getParameter("transportadora"); // Ej: "Servientrega", "Coordinadora"
            String guia            = request.getParameter("guia");           // Número de guía de la transportadora
            String fechaEstStr     = request.getParameter("fechaEstimada");  // Fecha estimada en formato YYYY-MM-DD
            String estadoStr       = request.getParameter("estado");         // Valor del enum EstadoEntrega como string

            // El ID del pedido es obligatorio en todas las operaciones
            if (idPedidoStr == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // Responde 400 Bad Request
                out.print("{\"error\":\"idPedido es obligatorio\"}");
                return;
            }

            int idPedido = Integer.parseInt(idPedidoStr); // Convierte el ID de String a entero
            PedidoJpaController pedidoCtrl = new PedidoJpaController(); // Controlador JPA del pedido
            Pedido pedido = pedidoCtrl.findPedido(idPedido); // Busca el pedido en BD por su ID
            if (pedido == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND); // Responde 404 Not Found
                out.print("{\"error\":\"Pedido no encontrado\"}");
                return;
            }

            em = JpaProvider.getEntityManagerFactory().createEntityManager(); // Abre la conexión con la BD
            EnvioJpaController envioCtrl = new EnvioJpaController(); // Controlador JPA del envío

            // ACTUALIZAR: cambiar el estado del envío existente
            if ("actualizar".equals(accion)) {
                String idEnvioStr = request.getParameter("idEnvio"); // ID del envío a actualizar
                if (idEnvioStr == null || estadoStr == null) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"idEnvio y estado son obligatorios\"}");
                    return;
                }
                Envio envio = envioCtrl.findEnvio(Integer.parseInt(idEnvioStr)); // Busca el envío en BD
                if (envio == null) { out.print("{\"error\":\"Envío no encontrado\"}"); return; }
                EstadoEntrega nuevoEstado = EstadoEntrega.valueOf(estadoStr); // Convierte texto a enum (ej: "ENTREGADO" → EstadoEntrega.ENTREGADO)
                envio.setEstadoEntrega(nuevoEstado); // Actualiza el estado del envío
                envio.setUpdatedAt(LocalDateTime.now()); // Actualiza el timestamp de última modificación
                if (guia != null && !guia.isBlank()) envio.setNumeroGuia(guia); // Actualiza la guía si se envió una nueva
                // Si el envío pasa a ENTREGADO, actualizar también el estado del pedido automáticamente
                if (nuevoEstado == EstadoEntrega.ENTREGADO) {
                    em.getTransaction().begin(); // Inicia la transacción
                    Pedido p = em.find(Pedido.class, idPedido); // Recarga el pedido dentro de la transacción
                    if (p != null) { p.setEstado(EstadoPedido.ENTREGADO); p.setUpdatedAt(LocalDateTime.now()); } // Cambia el estado del pedido a ENTREGADO
                    em.getTransaction().commit(); // Confirma el cambio de estado del pedido
                }
                envioCtrl.edit(envio); // UPDATE en BD (actualiza el envío)
                out.print("{\"ok\":true}");
                return; // Sale del método
            }

            // CREAR: registrar un nuevo envío para el pedido
            // Verificar que el pedido no tenga ya un envío registrado (solo puede tener uno)
            TypedQuery<Long> checkQ = em.createQuery(
                "SELECT COUNT(e) FROM Envio e WHERE e.pedido.idPedido = :id", Long.class); // Cuenta envíos de este pedido
            checkQ.setParameter("id", idPedido);
            if (checkQ.getSingleResult() > 0) {
                response.setStatus(HttpServletResponse.SC_CONFLICT); // HTTP 409 Conflict
                out.print("{\"error\":\"Este pedido ya tiene un envío registrado\"}");
                return; // No se puede registrar un segundo envío
            }

            // Validar que la dirección de envío no esté vacía
            if (direccion == null || direccion.isBlank()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"La dirección de envío es obligatoria\"}");
                return;
            }

            // Validar que el número de guía tenga entre 10 y 22 caracteres (rango estándar de guías de transporte)
            if (guia == null || guia.isBlank() || guia.trim().length() < 10 || guia.trim().length() > 22) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"El número de guía es obligatorio y debe tener entre 10 y 22 caracteres\"}");
                return;
            }

            // Verificar que el número de guía no esté ya registrado en otro envío (debe ser único)
            EntityManager emGuia = JpaProvider.getEntityManagerFactory().createEntityManager(); // EntityManager separado para esta consulta
            try {
                TypedQuery<Long> qGuia = emGuia.createQuery(
                    "SELECT COUNT(e) FROM Envio e WHERE e.numeroGuia = :guia", Long.class); // Busca si ya existe la guía
                qGuia.setParameter("guia", guia.trim());
                if (qGuia.getSingleResult() > 0) {
                    response.setStatus(HttpServletResponse.SC_CONFLICT); // HTTP 409 Conflict
                    out.print("{\"error\":\"El número de guía ya fue registrado en otro envío\"}");
                    return; // Guía duplicada
                }
            } finally {
                emGuia.close(); // Cierra el EntityManager auxiliar
            }

            // Construir el objeto Envio con todos los datos recibidos
            Envio envio = new Envio();
            envio.setPedido(pedido); // Asocia el envío al pedido
            envio.setDireccionEnvio(direccion); // Dirección de entrega
            envio.setTransportadora(transportadora != null ? transportadora : ""); // Nombre de la transportadora
            envio.setNumeroGuia(guia != null ? guia : ""); // Número de guía
            envio.setFechaEnvio(LocalDateTime.now()); // Fecha actual como fecha de despacho
            envio.setEstadoEntrega(EstadoEntrega.PREPARANDO); // Estado inicial del envío al crearlo
            envio.setCreatedAt(LocalDateTime.now()); // Timestamp de creación
            envio.setUpdatedAt(LocalDateTime.now()); // Timestamp de última modificación
            envio.setActivo(true); // El envío nuevo se crea activo

            // Parsear la fecha estimada de entrega si fue enviada (formato YYYY-MM-DD)
            if (fechaEstStr != null && !fechaEstStr.isBlank()) {
                try {
                    envio.setFechaEstimadaEntrega(LocalDate.parse(fechaEstStr).atStartOfDay()); // Convierte la fecha a LocalDateTime al inicio del día
                } catch (Exception ignored) {} // Si el formato es inválido, ignorar y dejar null
            }

            envioCtrl.create(envio); // INSERT en BD (registra el nuevo envío)

            // Cambiar el estado del pedido a ENVIADO automáticamente al registrar el envío
            em.getTransaction().begin(); // Inicia la transacción
            Pedido pedidoMerge = em.find(Pedido.class, idPedido); // Recarga el pedido dentro de la transacción activa
            if (pedidoMerge != null) {
                pedidoMerge.setEstado(EstadoPedido.ENVIADO); // Cambia el estado del pedido a ENVIADO
                pedidoMerge.setUpdatedAt(LocalDateTime.now()); // Actualiza el timestamp del pedido
            }
            em.getTransaction().commit(); // Confirma el cambio de estado del pedido en BD

            out.print("{\"ok\":true}"); // Confirma al frontend que el envío fue creado exitosamente

        } catch (Exception ex) {
            if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback(); // Si la transacción quedó abierta por el error, se revierte
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // Responde 500 Internal Server Error
            out.print("{\"error\":\"" + escapeJson(ex.getMessage()) + "\"}");
        } finally {
            if (em != null && em.isOpen()) em.close(); // Siempre cierra el EntityManager para evitar leaks de conexión
        }
    }

    // Escapa caracteres especiales para que no rompan el JSON de salida
    private String escapeJson(String s) {
        if (s == null) return ""; // Si el string es null retorna vacío
        return s.replace("\\", "\\\\") // Escapa backslashes
                .replace("\"", "\\\"") // Escapa comillas dobles
                .replace("\n", "\\n").replace("\r", "\\r"); // Escapa saltos de línea
    }
}
