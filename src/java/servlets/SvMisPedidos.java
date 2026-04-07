package servlets;                                        // declara que esta clase pertenece al paquete "servlets"

import java.io.IOException;                              // excepción de entrada/salida
import java.io.PrintWriter;                              // escribe texto en la respuesta HTTP
import java.util.List;                                   // lista de pedidos, detalles y envíos
import javax.persistence.EntityManager;                  // gestiona la sesión activa con la BD
import javax.persistence.TypedQuery;                     // consulta JPQL con tipo de retorno definido
import javax.servlet.ServletException;                   // excepción propia de los servlets
import javax.servlet.annotation.WebServlet;              // anotación que mapea la URL al servlet
import javax.servlet.http.HttpServlet;                   // clase base de todos los servlets HTTP
import javax.servlet.http.HttpServletRequest;            // representa la petición HTTP entrante
import javax.servlet.http.HttpServletResponse;           // representa la respuesta HTTP saliente
import logica.Detallepedido;                             // entidad que representa una línea del pedido (un producto comprado)
import logica.Envio;                                     // entidad que representa el envío del pedido
import logica.Pedido;                                    // entidad que representa el pedido completo
import logica.Usuario;                                   // entidad del usuario para identificar al cliente en sesión
import persistencias.JpaProvider;                        // Singleton que provee la EntityManagerFactory

/**
 * SvMisPedidos — Servlet que devuelve los pedidos del cliente logueado.
 * GET: retorna un array JSON con todos los pedidos del usuario en sesión,
 *      ordenados del más reciente al más antiguo.
 * Cada pedido incluye: id, estado, fecha, total, detalles (productos) y envio.
 * Es usado por la sección "MIS PEDIDOS" en perfil.jsp.
 * Requiere sesión activa.
 */
@WebServlet(name = "SvMisPedidos", urlPatterns = {"/SvMisPedidos"}) // cuando llegue a /SvMisPedidos, Tomcat ejecuta esta clase
public class SvMisPedidos extends HttpServlet {          // extiende HttpServlet para manejar peticiones HTTP

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) // se ejecuta cuando perfil.jsp carga la sección "Mis Pedidos"
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8"); // respuesta JSON en UTF-8
        response.setCharacterEncoding("UTF-8");          // codificación de la respuesta
        PrintWriter out = response.getWriter();          // escritor de la respuesta

        try {
            // Obtener el usuario desde la sesión HTTP activa
            javax.servlet.http.HttpSession sess = request.getSession(false); // false: no crear sesión nueva
            Usuario usuario = (sess != null) ? (Usuario) sess.getAttribute("usuario") : null; // usuario de la sesión

            // Si no hay sesión activa, retornar señal para que el frontend redirija al login
            if (usuario == null) {
                out.print("{\"error\":\"no-session\"}"); // código que el JS de perfil.jsp reconoce para redirigir
                return;
            }

            EntityManager em = JpaProvider.getEntityManagerFactory().createEntityManager(); // abre sesión con la BD
            try {
                // Consultar todos los pedidos del cliente asociado al usuario logueado
                // Se ordenan del más reciente al más antiguo (ORDER BY fechaPedido DESC)
                // Si el cliente está cargado en sesión, se usa directamente su ID
                // Si no, se recarga desde BD para evitar LazyInitializationException
                TypedQuery<Pedido> q = em.createQuery(
                    "SELECT p FROM Pedido p WHERE p.cliente.idCliente = :cid ORDER BY p.fechaPedido DESC",
                    Pedido.class);                       // filtra por id_cliente del usuario logueado
                q.setParameter("cid", usuario.getCliente() != null     // si el cliente está en sesión
                    ? usuario.getCliente().getIdCliente()               // usar el ID del cliente de sesión
                    : em.find(Usuario.class, usuario.getIdUsuario()).getCliente().getIdCliente()); // si no, recargar desde BD
                List<Pedido> pedidos = q.getResultList(); // lista de pedidos del cliente, del más reciente al más antiguo

                StringBuilder sb = new StringBuilder("["); // inicia el array JSON de pedidos
                for (int i = 0; i < pedidos.size(); i++) { // iterar sobre cada pedido del cliente
                    Pedido p = pedidos.get(i);           // pedido actual
                    if (i > 0) sb.append(",");           // separador entre objetos del array
                    sb.append("{");
                    sb.append("\"id\":").append(p.getIdPedido()).append(","); // ID del pedido
                    sb.append("\"estado\":\"").append(p.getEstado() != null ? p.getEstado().name() : "PENDIENTE").append("\","); // ej: "ENVIADO"
                    // Formatear la fecha como "YYYY-MM-DD HH:MM" sin segundos ni milisegundos
                    sb.append("\"fecha\":\"").append(p.getFechaPedido() != null ? p.getFechaPedido().toString().replace("T", " ").substring(0, 16) : "").append("\","); // ej: "2025-03-15 14:30"
                    sb.append("\"total\":").append(p.getTotal() != null ? p.getTotal() : 0).append(","); // monto total del pedido

                    // Consultar los productos de este pedido (cada línea de detalle)
                    sb.append("\"detalles\":[");         // inicia el array de productos comprados
                    TypedQuery<Detallepedido> dq = em.createQuery(
                        "SELECT d FROM Detallepedido d WHERE d.pedido.idPedido = :pid", Detallepedido.class); // buscar los ítems del pedido
                    dq.setParameter("pid", p.getIdPedido()); // :pid = ID del pedido actual
                    List<Detallepedido> detalles = dq.getResultList(); // lista de líneas de detalle
                    for (int j = 0; j < detalles.size(); j++) { // iterar sobre cada línea de detalle
                        Detallepedido d = detalles.get(j); // línea actual
                        if (j > 0) sb.append(",");
                        sb.append("{");
                        sb.append("\"producto\":\"").append(esc(d.getProducto() != null ? d.getProducto().getNombreProducto() : "Producto")).append("\","); // nombre del producto
                        sb.append("\"cantidad\":").append(d.getCantidad()).append(","); // cantidad comprada
                        sb.append("\"precioUnitario\":").append(d.getPrecioUnitario() != null ? d.getPrecioUnitario() : 0).append(","); // precio unitario al momento de la compra
                        sb.append("\"subtotal\":").append(d.getSubtotal()); // precio * cantidad = subtotal
                        sb.append("}");
                    }
                    sb.append("],");                     // cierra el array de detalles (nota la coma: seguirá el campo "envio")

                    // Consultar el envío del pedido para que el cliente vea el seguimiento
                    TypedQuery<Envio> eq = em.createQuery(
                        "SELECT e FROM Envio e WHERE e.pedido.idPedido = :pid", Envio.class); // buscar el envío del pedido
                    eq.setParameter("pid", p.getIdPedido()); // :pid = ID del pedido actual
                    List<Envio> envios = eq.getResultList(); // 0 o 1 envío por pedido
                    if (!envios.isEmpty()) {             // si el pedido ya fue despachado
                        Envio e = envios.get(0);         // obtener los datos del envío
                        sb.append("\"envio\":{");
                        sb.append("\"transportadora\":\"").append(esc(e.getTransportadora())).append("\","); // ej: "Coordinadora"
                        sb.append("\"guia\":\"").append(esc(e.getNumeroGuia())).append("\",");              // número de guía
                        sb.append("\"estado\":\"").append(e.getEstadoEntrega() != null ? e.getEstadoEntrega().name() : "").append("\","); // ej: "EN_CAMINO"
                        // Usar solo la parte de fecha (sin hora) para presentación en la UI del cliente
                        sb.append("\"fechaEnvio\":\"").append(e.getFechaEnvio() != null ? e.getFechaEnvio().toLocalDate().toString() : "").append("\","); // YYYY-MM-DD
                        sb.append("\"fechaEstimada\":\"").append(e.getFechaEstimadaEntrega() != null ? e.getFechaEstimadaEntrega().toLocalDate().toString() : "").append("\""); // YYYY-MM-DD
                        sb.append("}");
                    } else {
                        // Sin envío: el pedido está siendo procesado internamente
                        sb.append("\"envio\":null");     // null indica que no hay envío aún
                    }
                    sb.append("}");                      // cierra el objeto de este pedido
                }
                sb.append("]");                          // cierra el array de pedidos
                out.print(sb.toString());                // envía el JSON al navegador
            } finally {
                if (em.isOpen()) em.close();             // SIEMPRE cierra el EntityManager
            }

        } catch (Exception e) {
            // Capturar el stack trace completo para facilitar el diagnóstico del error
            java.io.StringWriter sw = new java.io.StringWriter();
            e.printStackTrace(new java.io.PrintWriter(sw));
            // Limitar a 300 caracteres para no saturar la respuesta JSON
            out.print("{\"error\":\"" + esc(sw.toString().substring(0, Math.min(300, sw.toString().length()))) + "\"}");
        }
    }

    // Método auxiliar que escapa caracteres especiales para JSON
    // Convierte saltos de línea en espacios para que el JSON no se rompa
    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", " ").replace("\r", "");   // saltos de línea → espacio
    }
}
