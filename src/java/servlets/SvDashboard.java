package servlets;                                        // declara que esta clase pertenece al paquete "servlets"

import java.io.IOException;                              // excepción de entrada/salida (requerida por doGet)
import java.io.PrintWriter;                              // escribe texto en la respuesta HTTP
import java.math.BigDecimal;                             // tipo numérico exacto para sumas de dinero (sin errores de punto flotante)
import java.util.List;                                   // lista de pedidos recientes
import javax.persistence.EntityManager;                  // gestiona la sesión activa con la BD
import javax.persistence.Query;                          // consulta JPA nativa; reemplaza TypedQuery en este servlet
import javax.servlet.ServletException;                   // excepción propia de los servlets
import javax.servlet.annotation.WebServlet;              // anotación que mapea la URL al servlet
import javax.servlet.http.HttpServlet;                   // clase base de todos los servlets HTTP
import javax.servlet.http.HttpServletRequest;            // representa la petición HTTP entrante
import javax.servlet.http.HttpServletResponse;           // representa la respuesta HTTP saliente
import logica.Pedido;                                    // entidad que representa un pedido en la tabla pedido
import persistencias.JpaProvider;                        // Singleton que provee la EntityManagerFactory
import persistencias.PedidoJpaController;                // controlador para consultar conteo de pedidos
import persistencias.ProductoJpaController;              // controlador para consultar conteo de productos
import persistencias.UsuarioJpaController;               // controlador para consultar conteo de usuarios

/**
 * SvDashboard — Servlet que provee datos para el panel principal del administrador.
 * GET: retorna un JSON con las métricas generales del sistema:
 *   - ventas: suma de los totales de todos los pedidos excepto los cancelados
 *   - productos: cantidad total de productos
 *   - pedidos: cantidad total de pedidos
 *   - usuarios: cantidad total de usuarios
 *   - pedidosRecientes: los 5 pedidos más recientes con cliente, total y estado
 * Requiere el permiso VER_DASHBOARD.
 *
 * TODAS las consultas a BD usan createNativeQuery con SQL puro y parámetros posicionales (?).
 * EclipseLink no soporta parámetros nombrados (:x) en native queries → siempre usamos ?.
 */
@WebServlet(name = "SvDashboard", urlPatterns = {"/SvDashboard"}) // cuando llegue una petición a /SvDashboard, Tomcat ejecuta esta clase
public class SvDashboard extends HttpServlet {           // extiende HttpServlet para manejar peticiones HTTP

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) // se ejecuta cuando admin.js llama a fetch('/SvDashboard')
            throws ServletException, IOException {       // declara las excepciones que puede lanzar

        response.setContentType("application/json;charset=UTF-8"); // indica al navegador que la respuesta es JSON en UTF-8
        PrintWriter out = response.getWriter();          // obtiene el escritor para enviar texto en la respuesta

        // Verificar que el usuario tiene el permiso VER_DASHBOARD antes de retornar datos sensibles
        if (!AuthHelper.tienePermiso(request, "VER_DASHBOARD")) { // lee la lista de permisos de la sesión HTTP
            response.setStatus(HttpServletResponse.SC_FORBIDDEN); // código HTTP 403: acceso denegado
            out.print("{\"error\":\"Sin permiso: VER_DASHBOARD\"}"); // mensaje JSON de error
            return;                                      // detiene la ejecución; no procesar ni devolver datos
        }

        EntityManager em = null;                         // declara em en null para poder cerrarlo en el finally
        try {                                            // bloque protegido: si algo falla se captura abajo
            em = JpaProvider.getEntityManagerFactory().createEntityManager(); // abre una sesión con la BD

            // Obtener contadores usando los métodos count() de cada controlador.
            // PedidoJpaController.getPedidoCount() usa internamente:
            //   createNativeQuery("SELECT COUNT(*) FROM pedido")
            long totalProductos = new ProductoJpaController().getProductoCount(); // SELECT COUNT(*) FROM producto
            long totalUsuarios  = new UsuarioJpaController().getUsuarioCount();   // SELECT COUNT(*) FROM usuario
            long totalPedidos   = new PedidoJpaController().getPedidoCount();     // SELECT COUNT(*) FROM pedido

            // Consulta nativa para sumar los totales de todos los pedidos EXCEPTO los cancelados.
            // Reemplaza el JPQL: "SELECT COALESCE(SUM(p.total), 0) FROM Pedido p WHERE p.estado <> enums.EstadoPedido.CANCELADO"
            // En SQL nativo el enum se compara directamente con su valor de texto en BD: estado <> 'CANCELADO'.
            // COALESCE(SUM(total), 0): si no hay pedidos, SUM retorna NULL; COALESCE lo convierte a 0.
            // El resultado puede llegar como BigDecimal, Double o Integer según el driver;
            // new BigDecimal(toString()) garantiza la conversión sin importar el tipo retornado.
            Query qVentas = em.createNativeQuery(
                "SELECT COALESCE(SUM(total), 0) FROM pedido WHERE estado <> 'CANCELADO'");
            BigDecimal ventas = new BigDecimal(qVentas.getSingleResult().toString()); // obtiene el único escalar y lo convierte a BigDecimal

            // Consulta nativa para los 5 pedidos más recientes ordenados por fecha descendente.
            // Reemplaza el JPQL: "SELECT p FROM Pedido p ORDER BY COALESCE(p.fechaPedido, p.createdAt) DESC"
            // con setMaxResults(5) (que en JPQL agrega LIMIT al SQL generado).
            // En SQL nativo el ORDER BY usa los nombres de columna reales: fecha_pedido y created_at.
            // COALESCE(fecha_pedido, created_at): usa fecha_pedido si existe, si no usa created_at como respaldo.
            // LIMIT 5 al final del SQL nativo reemplaza setMaxResults(5) que se usaba con TypedQuery.
            // Pedido.class: EclipseLink mapea automáticamente cada fila al objeto Pedido.
            Query qRecientes = em.createNativeQuery(
                "SELECT * FROM pedido ORDER BY COALESCE(fecha_pedido, created_at) DESC LIMIT 5",
                Pedido.class);                           // Pedido.class = mapeo automático de filas a entidades
            List<Pedido> recientes = qRecientes.getResultList(); // ejecuta el SELECT y retorna la lista de los 5 pedidos más recientes

            // Construir el objeto JSON de respuesta para el dashboard
            StringBuilder sb = new StringBuilder("{");   // inicia el objeto JSON raíz con {
            sb.append("\"ventas\":").append(ventas != null ? ventas : BigDecimal.ZERO).append(",");   // total de ventas en dinero
            sb.append("\"productos\":").append(totalProductos).append(","); // cantidad total de productos en catálogo
            sb.append("\"pedidos\":").append(totalPedidos).append(",");     // cantidad total de pedidos registrados
            sb.append("\"usuarios\":").append(totalUsuarios).append(",");   // cantidad total de cuentas de usuario
            sb.append("\"pedidosRecientes\":[");         // inicia el array de pedidos recientes
            for (int i = 0; i < recientes.size(); i++) { // iterar sobre cada pedido reciente (máximo 5)
                Pedido p = recientes.get(i);             // pedido actual
                if (i > 0) sb.append(",");               // separador entre objetos JSON del array
                sb.append("{");
                sb.append("\"id\":").append(p.getIdPedido()).append(","); // ID del pedido
                sb.append("\"cliente\":\"").append(
                    p.getCliente() != null ? escapeJson(p.getCliente().getNombreCompleto()) : "" // nombre del cliente o vacío si no tiene
                ).append("\",");
                sb.append("\"total\":").append(p.getTotal() != null ? p.getTotal() : 0).append(","); // monto total del pedido
                // Usar fechaPedido si está disponible, si no usar createdAt como respaldo
                sb.append("\"fecha\":\"").append(
                    p.getFechaPedido() != null ? p.getFechaPedido().toString() :                   // fecha del pedido si existe
                    (p.getCreatedAt() != null ? p.getCreatedAt().toString() : "")                  // si no, fecha de creación en BD
                ).append("\",");
                sb.append("\"estado\":\"").append(p.getEstado() != null ? p.getEstado().name() : "").append("\""); // ej: "PENDIENTE", "ENVIADO"
                sb.append("}");                          // cierra el objeto de este pedido
            }
            sb.append("]}");                             // cierra el array de pedidos recientes y el objeto JSON raíz
            out.print(sb.toString());                    // envía el JSON al navegador

        } catch (Exception e) {                          // captura cualquier excepción inesperada (BD caída, etc.)
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // código HTTP 500
            out.print("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}"); // mensaje de error genérico al admin
        } finally {
            if (em != null && em.isOpen()) em.close();   // SIEMPRE cierra el EntityManager para devolver la conexión al pool
        }
    }

    // Método auxiliar que escapa caracteres especiales en strings que irán dentro de un JSON
    private String escapeJson(String s) {
        if (s == null) return "";                        // si es null retorna cadena vacía
        return s.replace("\\", "\\\\").replace("\"", "\\\"") // escapa barras y comillas dobles
                .replace("\n", "\\n").replace("\r", "\\r");  // escapa saltos de línea
    }
}
