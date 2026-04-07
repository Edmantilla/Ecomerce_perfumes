package servlets;                                        // declara que esta clase pertenece al paquete "servlets"

import enums.EstadoPedido;                               // enum con los estados posibles: PENDIENTE, ENVIADO, ENTREGADO, CANCELADO, PAGO
import java.io.IOException;                              // excepción de entrada/salida
import java.io.PrintWriter;                              // escribe texto en la respuesta HTTP
import java.time.LocalDateTime;                          // fecha+hora para updated_at al cambiar estado
import java.util.List;                                   // lista de pedidos y teléfonos
import javax.persistence.EntityManager;                  // gestiona la sesión activa con la BD
import javax.persistence.TypedQuery;                     // consulta JPQL con tipo de retorno definido
import javax.servlet.ServletException;                   // excepción propia de los servlets
import javax.servlet.annotation.WebServlet;              // anotación que mapea la URL al servlet
import javax.servlet.http.HttpServlet;                   // clase base de todos los servlets HTTP
import javax.servlet.http.HttpServletRequest;            // representa la petición HTTP entrante
import javax.servlet.http.HttpServletResponse;           // representa la respuesta HTTP saliente
import logica.Pedido;                                    // entidad que representa un pedido
import logica.Telefonocliente;                           // entidad que representa un teléfono del cliente
import logica.Usuario;                                   // entidad del usuario (no usada directamente pero importada por convención)
import persistencias.JpaProvider;                        // Singleton que provee la EntityManagerFactory
import persistencias.PedidoJpaController;                // controlador para CRUD en la tabla pedido

/**
 * SvPedidos — Servlet de gestión de pedidos para el panel admin.
 * GET: lista todos los pedidos con datos del cliente (requiere VER_PEDIDOS).
 * POST accion=cambiarEstado: actualiza el estado de un pedido (requiere GESTIONAR_PEDIDOS).
 */
@WebServlet(name = "SvPedidos", urlPatterns = {"/SvPedidos"}) // cuando llegue una petición a /SvPedidos, Tomcat ejecuta esta clase
public class SvPedidos extends HttpServlet {             // extiende HttpServlet para manejar peticiones HTTP

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) // se ejecuta cuando admin.js hace GET a /SvPedidos
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8"); // respuesta JSON en UTF-8
        PrintWriter out = response.getWriter();          // escritor de la respuesta

        // Verificar que el usuario tiene permiso para ver los pedidos
        if (!AuthHelper.tienePermiso(request, "VER_PEDIDOS")) {  // consulta la sesión HTTP
            response.setStatus(HttpServletResponse.SC_FORBIDDEN); // HTTP 403
            out.print("{\"error\":\"Sin permiso: VER_PEDIDOS\"}");
            return;
        }

        EntityManager emGet = null;                      // EntityManager para consultar teléfonos de cada cliente
        try {
            PedidoJpaController ctrl = new PedidoJpaController(); // controlador para consultar todos los pedidos
            List<Pedido> pedidos = ctrl.findPedidoEntities(); // SELECT * FROM pedido ORDER BY id_pedido ASC
            // EntityManager separado para consultar teléfonos de cada cliente sin conflicto de sesión
            emGet = JpaProvider.getEntityManagerFactory().createEntityManager(); // abre sesión para consultas auxiliares

            // Construir el array JSON de pedidos con los datos del cliente incluidos
            StringBuilder sb = new StringBuilder("[");   // inicia el array JSON
            for (int i = 0; i < pedidos.size(); i++) {   // iterar sobre cada pedido
                Pedido p = pedidos.get(i);               // pedido actual
                if (i > 0) sb.append(",");               // separador entre objetos del array
                sb.append("{");
                sb.append("\"id\":").append(p.getIdPedido()).append(","); // ID del pedido
                sb.append("\"estado\":\"").append(esc(p.getEstado() != null ? p.getEstado().name() : "")).append("\","); // ej: "PENDIENTE"
                sb.append("\"fecha\":\"").append(p.getFechaPedido() != null ? p.getFechaPedido().toString() : "").append("\","); // fecha de la compra
                sb.append("\"total\":").append(p.getTotal() != null ? p.getTotal() : 0).append(","); // monto total del pedido
                sb.append("\"cliente\":\"").append(
                    p.getCliente() != null ? esc(p.getCliente().getNombreCompleto()) : "" // nombre del cliente o vacío
                ).append("\",");
                if (p.getCliente() != null) {            // si el pedido tiene un cliente asociado
                    int idC = p.getCliente().getIdCliente(); // ID del cliente para consultar sus datos
                    String dir = p.getCliente().getDireccion(); // dirección de entrega del cliente
                    sb.append("\"direccionCliente\":\"").append(esc(dir != null ? dir : "")).append("\","); // dirección para el envío
                    // Consultar los teléfonos activos del cliente para mostrarlos en el panel admin
                    List<Telefonocliente> tels = emGet.createQuery(
                        "SELECT t FROM Telefonocliente t WHERE t.cliente.idCliente = :id AND t.activo = true ORDER BY t.idTelefono",
                        Telefonocliente.class).setParameter("id", idC).getResultList(); // SELECT teléfonos activos del cliente
                    sb.append("\"telefonosCliente\":[");  // inicia el array de teléfonos
                    for (int j = 0; j < tels.size(); j++) { // iterar sobre los teléfonos del cliente
                        if (j > 0) sb.append(",");
                        sb.append("\"").append(esc(tels.get(j).getTelefono())).append("\""); // número de teléfono
                    }
                    sb.append("]");                      // cierra el array de teléfonos
                } else {
                    // Pedido sin cliente asociado (caso inusual, posible si el cliente fue eliminado)
                    sb.append("\"direccionCliente\":\"\",\"telefonosCliente\":[]" );
                }
                sb.append("}");                          // cierra el objeto de este pedido
            }
            sb.append("]");                              // cierra el array JSON
            out.print(sb.toString());                    // envía el JSON al navegador

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"" + esc(e.getMessage()) + "\"}");
        } finally {
            if (emGet != null && emGet.isOpen()) emGet.close(); // SIEMPRE cierra el EntityManager
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) // se ejecuta cuando admin.js hace POST a /SvPedidos
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");          // codificación de la respuesta
        PrintWriter out = response.getWriter();

        try {
            // Verificar que el usuario tiene permiso para gestionar pedidos
            if (!AuthHelper.tienePermiso(request, "GESTIONAR_PEDIDOS")) { // permiso para cambiar estados
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print("{\"error\":\"Sin permiso: GESTIONAR_PEDIDOS\"}");
                return;
            }

            String accion = request.getParameter("accion"); // actualmente solo "cambiarEstado" está implementado

            if ("cambiarEstado".equals(accion)) {        // acción de cambio de estado de un pedido
                String idStr     = request.getParameter("idPedido"); // ID del pedido a actualizar
                String estadoStr = request.getParameter("estado");   // nuevo estado (debe coincidir con EstadoPedido enum)

                if (idStr == null || estadoStr == null) { // validar que ambos parámetros estén presentes
                    out.print("{\"error\":\"Parámetros faltantes\"}");
                    return;
                }

                int idPedido = Integer.parseInt(idStr.trim()); // parsear el ID del pedido
                // Convertir el texto recibido al valor del enum EstadoPedido
                // Lanza IllegalArgumentException si el texto no coincide con ningún valor del enum
                EstadoPedido nuevoEstado = EstadoPedido.valueOf(estadoStr.trim().toUpperCase()); // ej: "enviado" → EstadoPedido.ENVIADO

                EntityManager em = JpaProvider.getEntityManagerFactory().createEntityManager(); // abre sesión para el UPDATE
                try {
                    em.getTransaction().begin();         // inicia transacción para el UPDATE
                    Pedido pedido = em.find(Pedido.class, idPedido); // SELECT * FROM pedido WHERE id_pedido = ?
                    if (pedido == null) {                // si el pedido no existe en BD
                        em.getTransaction().rollback();  // no hay nada que guardar, revertir
                        out.print("{\"error\":\"Pedido no encontrado\"}");
                        return;
                    }
                    // Actualizar el estado y registrar la fecha de la modificación
                    pedido.setEstado(nuevoEstado);       // asignar el nuevo estado al pedido
                    pedido.setUpdatedAt(LocalDateTime.now()); // actualizar la marca de tiempo de última modificación
                    em.merge(pedido);                    // UPDATE pedido SET estado=?, updated_at=? WHERE id_pedido=?
                    em.getTransaction().commit();        // confirmar el cambio en MySQL
                    out.print("{\"ok\":true,\"idPedido\":" + idPedido + ",\"estado\":\"" + nuevoEstado.name() + "\"}"); // notificar el nuevo estado al frontend
                } finally {
                    if (em.isOpen()) em.close();         // SIEMPRE cierra el EntityManager
                }
            } else {
                out.print("{\"error\":\"Acción desconocida\"}"); // acción no reconocida
            }

        } catch (IllegalArgumentException e) {           // captura específica: el texto del estado no existe en el enum
            out.print("{\"error\":\"Estado inválido: " + esc(e.getMessage()) + "\"}");
        } catch (Exception e) {                          // captura general: error de BD u otro inesperado
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"" + esc(e.getMessage()) + "\"}");
        }
    }

    // Método auxiliar que escapa caracteres especiales para JSON
    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }
}
