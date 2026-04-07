package servlets;                                        // declara que esta clase pertenece al paquete "servlets"

import java.io.IOException;                              // excepción de entrada/salida
import java.io.PrintWriter;                              // escribe texto en la respuesta HTTP
import java.util.List;                                   // lista de detalles y envíos
import javax.persistence.EntityManager;                  // gestiona la sesión activa con la BD
import javax.persistence.TypedQuery;                     // consulta JPQL con tipo de retorno definido
import javax.servlet.ServletException;                   // excepción propia de los servlets
import javax.servlet.annotation.WebServlet;              // anotación que mapea la URL al servlet
import javax.servlet.http.HttpServlet;                   // clase base de todos los servlets HTTP
import javax.servlet.http.HttpServletRequest;            // representa la petición HTTP entrante
import javax.servlet.http.HttpServletResponse;           // representa la respuesta HTTP saliente
import logica.Detallepedido;                             // entidad que representa una línea del pedido (un producto comprado)
import logica.Envio;                                     // entidad que representa el envío asociado al pedido
import persistencias.JpaProvider;                        // Singleton que provee la EntityManagerFactory

/**
 * SvDetallesPedido — Servlet que devuelve el detalle completo de un pedido específico.
 * GET ?idPedido=X: retorna un objeto JSON con:
 *   - items: lista de productos comprados (nombre, cantidad, precio unitario, subtotal)
 *   - envio: información del envío si existe (transportadora, guía, fechas, estado)
 * Es usado por el modal de detalle de pedido en el panel admin.
 * Requiere el permiso VER_PEDIDOS.
 */
@WebServlet(name = "SvDetallesPedido", urlPatterns = {"/SvDetallesPedido"}) // cuando llegue a /SvDetallesPedido, Tomcat ejecuta esta clase
public class SvDetallesPedido extends HttpServlet {      // extiende HttpServlet para manejar peticiones HTTP

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) // se ejecuta cuando el admin hace clic en "Ver detalle" de un pedido
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8"); // respuesta JSON en UTF-8
        response.setCharacterEncoding("UTF-8");          // codificación de la respuesta
        PrintWriter out = response.getWriter();          // escritor de la respuesta

        try {
            // Verificar que el usuario tiene permiso para ver el detalle de los pedidos
            if (!AuthHelper.tienePermiso(request, "VER_PEDIDOS")) { // consulta la sesión HTTP
                response.setStatus(HttpServletResponse.SC_FORBIDDEN); // HTTP 403
                out.print("{\"error\":\"Sin permiso: VER_PEDIDOS\"}");
                return;
            }

            String idStr = request.getParameter("idPedido"); // ID del pedido del que se quiere ver el detalle
            if (idStr == null || idStr.isBlank()) {      // validar que el parámetro está presente
                out.print("{\"error\":\"idPedido requerido\"}");
                return;
            }

            int idPedido = Integer.parseInt(idStr.trim()); // parsear el ID como entero

            EntityManager em = JpaProvider.getEntityManagerFactory().createEntityManager(); // abre sesión con la BD
            try {
                // Consultar todos los productos (líneas de detalle) de este pedido
                TypedQuery<Detallepedido> q = em.createQuery(
                    "SELECT d FROM Detallepedido d WHERE d.pedido.idPedido = :pid", Detallepedido.class); // filtrar por el ID del pedido
                q.setParameter("pid", idPedido);         // :pid = ID del pedido que se está consultando
                List<Detallepedido> detalles = q.getResultList(); // lista de líneas de detalle (un ítem por fila)

                // Construir el array JSON de items (cada producto comprado en el pedido)
                StringBuilder items = new StringBuilder("["); // inicia el array de items
                for (int i = 0; i < detalles.size(); i++) { // iterar sobre cada línea de detalle
                    Detallepedido d = detalles.get(i);   // detalle actual
                    if (i > 0) items.append(",");        // separador entre objetos
                    items.append("{");
                    items.append("\"producto\":\"").append(esc(d.getProducto() != null ? d.getProducto().getNombreProducto() : "Producto")).append("\","); // nombre del producto
                    items.append("\"cantidad\":").append(d.getCantidad()).append(","); // cantidad comprada
                    items.append("\"precioUnitario\":").append(d.getPrecioUnitario() != null ? d.getPrecioUnitario() : 0).append(","); // precio al que se compró
                    items.append("\"subtotal\":").append(d.getSubtotal()); // precio * cantidad = subtotal de esta línea
                    items.append("}");
                }
                items.append("]");                       // cierra el array de items

                // Consultar el envío asociado al pedido
                // Un pedido puede no tener envío aún (si todavía no fue despachado)
                TypedQuery<Envio> qe = em.createQuery(
                    "SELECT e FROM Envio e WHERE e.pedido.idPedido = :pid", Envio.class); // buscar el envío del pedido
                qe.setParameter("pid", idPedido);        // :pid = ID del pedido
                List<Envio> envios = qe.getResultList(); // lista de envíos (0 o 1 elemento; un pedido tiene máximo un envío)

                // Construir el JSON del envío; si no existe, será null
                StringBuilder envioJson = new StringBuilder("null"); // valor por defecto si no hay envío
                if (!envios.isEmpty()) {                 // si existe un envío para este pedido
                    Envio e = envios.get(0);             // obtener el único envío
                    envioJson = new StringBuilder("{");
                    envioJson.append("\"transportadora\":\"").append(esc(e.getTransportadora())).append("\","); // ej: "Servientrega"
                    envioJson.append("\"guia\":\"").append(esc(e.getNumeroGuia())).append("\",");              // número de guía de rastreo
                    envioJson.append("\"estado\":\"").append(e.getEstadoEntrega() != null ? e.getEstadoEntrega().name() : "").append("\","); // ej: "PREPARANDO"
                    // Solo la parte de fecha (sin hora) para mostrar en el modal del admin
                    envioJson.append("\"fechaEnvio\":\"").append(e.getFechaEnvio() != null ? e.getFechaEnvio().toLocalDate().toString() : "").append("\","); // fecha de despacho YYYY-MM-DD
                    envioJson.append("\"fechaEstimada\":\"").append(e.getFechaEstimadaEntrega() != null ? e.getFechaEstimadaEntrega().toLocalDate().toString() : "").append("\""); // fecha estimada de entrega YYYY-MM-DD
                    envioJson.append("}");
                }

                // Retornar el objeto JSON completo: items (líneas de productos) + envio (datos de despacho)
                out.print("{\"items\":" + items.toString() + ",\"envio\":" + envioJson.toString() + "}"); // respuesta final

            } finally {
                if (em.isOpen()) em.close();             // SIEMPRE cierra el EntityManager
            }

        } catch (NumberFormatException e) {              // captura específica: el idPedido no era un número válido
            out.print("{\"error\":\"idPedido inv\u00e1lido\"}"); // \u00e1 = á (unicode)
        } catch (Exception e) {                          // captura general: error de BD u otro inesperado
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"" + esc(e.getMessage()) + "\"}");
        }
    }

    // Método auxiliar que escapa caracteres especiales para JSON
    // También convierte saltos de línea en espacios para no romper la estructura del JSON
    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", " ").replace("\r", "");   // saltos de línea → espacio (para strings que pueden tener enters)
    }
}
