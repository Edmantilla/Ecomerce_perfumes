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
import logica.Pedido;
import logica.Usuario;
import persistencias.JpaProvider;

/**
 * SvMisPedidos — Servlet que devuelve los pedidos del cliente logueado.
 * GET: retorna un array JSON con todos los pedidos del usuario en sesión,
 *      ordenados del más reciente al más antiguo.
 * Cada pedido incluye: id, estado, fecha, total, detalles (productos) y envio.
 * Es usado por la sección "MIS PEDIDOS" en perfil.jsp.
 * Requiere sesión activa.
 */
@WebServlet(name = "SvMisPedidos", urlPatterns = {"/SvMisPedidos"})
public class SvMisPedidos extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            // Obtener el usuario desde la sesión HTTP activa
            javax.servlet.http.HttpSession sess = request.getSession(false);
            Usuario usuario = (sess != null) ? (Usuario) sess.getAttribute("usuario") : null;

            // Si no hay sesión activa, el frontend redirige al login
            if (usuario == null) {
                out.print("{\"error\":\"no-session\"}");
                return;
            }

            EntityManager em = JpaProvider.getEntityManagerFactory().createEntityManager();
            try {
                // Consultar todos los pedidos del cliente del usuario logueado, ordenados del más reciente
                // Si el cliente ya está cargado en el objeto de sesión se usa directamente;
                // si no, se vuelve a cargar desde BD para evitar LazyInitializationException
                TypedQuery<Pedido> q = em.createQuery(
                    "SELECT p FROM Pedido p WHERE p.cliente.idCliente = :cid ORDER BY p.fechaPedido DESC",
                    Pedido.class);
                q.setParameter("cid", usuario.getCliente() != null
                    ? usuario.getCliente().getIdCliente()
                    : em.find(Usuario.class, usuario.getIdUsuario()).getCliente().getIdCliente());
                List<Pedido> pedidos = q.getResultList();

                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < pedidos.size(); i++) {
                    Pedido p = pedidos.get(i);
                    if (i > 0) sb.append(",");
                    sb.append("{");
                    sb.append("\"id\":").append(p.getIdPedido()).append(",");
                    sb.append("\"estado\":\"").append(p.getEstado() != null ? p.getEstado().name() : "PENDIENTE").append("\",");
                    sb.append("\"fecha\":\"").append(p.getFechaPedido() != null ? p.getFechaPedido().toString().replace("T", " ").substring(0, 16) : "").append("\",");
                    sb.append("\"total\":").append(p.getTotal() != null ? p.getTotal() : 0).append(",");

                    // Consultar los productos del pedido (cada línea de detalle)
                    sb.append("\"detalles\":[");
                    TypedQuery<Detallepedido> dq = em.createQuery(
                        "SELECT d FROM Detallepedido d WHERE d.pedido.idPedido = :pid", Detallepedido.class);
                    dq.setParameter("pid", p.getIdPedido());
                    List<Detallepedido> detalles = dq.getResultList();
                    for (int j = 0; j < detalles.size(); j++) {
                        Detallepedido d = detalles.get(j);
                        if (j > 0) sb.append(",");
                        sb.append("{");
                        sb.append("\"producto\":\"").append(esc(d.getProducto() != null ? d.getProducto().getNombreProducto() : "Producto")).append("\",");
                        sb.append("\"cantidad\":").append(d.getCantidad()).append(",");
                        sb.append("\"precioUnitario\":").append(d.getPrecioUnitario() != null ? d.getPrecioUnitario() : 0).append(",");
                        sb.append("\"subtotal\":").append(d.getSubtotal()); // precio * cantidad
                        sb.append("}");
                    }
                    sb.append("],");

                    // Consultar el envío del pedido (puede ser null si aún no se ha despachado)
                    TypedQuery<Envio> eq = em.createQuery(
                        "SELECT e FROM Envio e WHERE e.pedido.idPedido = :pid", Envio.class);
                    eq.setParameter("pid", p.getIdPedido());
                    List<Envio> envios = eq.getResultList();
                    if (!envios.isEmpty()) {
                        // Incluir los datos de envío para que el cliente vea el seguimiento
                        Envio e = envios.get(0);
                        sb.append("\"envio\":{");
                        sb.append("\"transportadora\":\"").append(esc(e.getTransportadora())).append("\",");
                        sb.append("\"guia\":\"").append(esc(e.getNumeroGuia())).append("\",");
                        sb.append("\"estado\":\"").append(e.getEstadoEntrega() != null ? e.getEstadoEntrega().name() : "").append("\",");
                        // Usar solo la parte de fecha (sin hora) para presentación en la UI
                        sb.append("\"fechaEnvio\":\"").append(e.getFechaEnvio() != null ? e.getFechaEnvio().toLocalDate().toString() : "").append("\",");
                        sb.append("\"fechaEstimada\":\"").append(e.getFechaEstimadaEntrega() != null ? e.getFechaEstimadaEntrega().toLocalDate().toString() : "").append("\"");
                        sb.append("}");
                    } else {
                        // Sin envío todavía: el pedido sigue en proceso interno
                        sb.append("\"envio\":null");
                    }
                    sb.append("}");
                }
                sb.append("]");
                out.print(sb.toString());
            } finally {
                if (em.isOpen()) em.close();
            }

        } catch (Exception e) {
            java.io.StringWriter sw = new java.io.StringWriter();
            e.printStackTrace(new java.io.PrintWriter(sw));
            out.print("{\"error\":\"" + esc(sw.toString().substring(0, Math.min(300, sw.toString().length()))) + "\"}");
        }
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", " ").replace("\r", "");
    }
}
