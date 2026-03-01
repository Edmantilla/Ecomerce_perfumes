package servlets;

import enums.TipoTelefono;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import logica.Cliente;
import logica.Correocliente;
import logica.Telefonocliente;
import logica.Usuario;
import persistencias.JpaProvider;

public class SvContactoCliente extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"error\":\"Sesi\u00f3n no iniciada\"}");
            return;
        }

        EntityManager em = null;
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            Cliente cliente = usuario.getCliente();
            if (cliente == null) {
                out.print("{\"telefonos\":[],\"correos\":[]}");
                return;
            }
            int idCliente = cliente.getIdCliente();
            em = JpaProvider.getEntityManagerFactory().createEntityManager();

            TypedQuery<Telefonocliente> qt = em.createQuery(
                "SELECT t FROM Telefonocliente t WHERE t.cliente.idCliente = :id AND t.activo = true ORDER BY t.idTelefono",
                Telefonocliente.class);
            qt.setParameter("id", idCliente);
            List<Telefonocliente> tels = qt.getResultList();

            TypedQuery<Correocliente> qc = em.createQuery(
                "SELECT c FROM Correocliente c WHERE c.cliente.idCliente = :id AND c.activo = true ORDER BY c.idCorreo",
                Correocliente.class);
            qc.setParameter("id", idCliente);
            List<Correocliente> correos = qc.getResultList();

            StringBuilder sb = new StringBuilder("{");
            sb.append("\"telefonos\":[");
            for (int i = 0; i < tels.size(); i++) {
                Telefonocliente t = tels.get(i);
                if (i > 0) sb.append(",");
                sb.append("{\"id\":").append(t.getIdTelefono()).append(",");
                sb.append("\"numero\":\"").append(escapeJson(t.getTelefono())).append("\",");
                sb.append("\"tipo\":\"").append(t.getTipoTelefono() != null ? t.getTipoTelefono().name() : "").append("\"}");
            }
            sb.append("],\"correos\":[");
            for (int i = 0; i < correos.size(); i++) {
                Correocliente c = correos.get(i);
                if (i > 0) sb.append(",");
                sb.append("{\"id\":").append(c.getIdCorreo()).append(",");
                sb.append("\"correo\":\"").append(escapeJson(c.getCorreo())).append("\",");
                sb.append("\"principal\":").append(c.isPrincipal()).append("}");
            }
            sb.append("]}");
            out.print(sb.toString());

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

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"error\":\"Sesi\u00f3n no iniciada\"}");
            return;
        }

        EntityManager em = null;
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            Cliente cliente = usuario.getCliente();
            if (cliente == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"El usuario no tiene cliente asociado\"}");
                return;
            }
            int idCliente = cliente.getIdCliente();

            String tipo   = request.getParameter("tipo");   // "telefono" | "correo"
            String accion = request.getParameter("accion"); // "agregar" | "eliminar"
            String idStr  = request.getParameter("id");

            em = JpaProvider.getEntityManagerFactory().createEntityManager();
            em.getTransaction().begin();

            Cliente clienteRef = em.find(Cliente.class, idCliente);

            if ("direccion".equals(tipo)) {
                String nuevaDir = request.getParameter("direccion");
                if (nuevaDir == null || nuevaDir.isBlank()) {
                    em.getTransaction().rollback();
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"La direcci\u00f3n no puede estar vac\u00eda\"}");
                    return;
                }
                clienteRef.setDireccion(nuevaDir.trim());
                em.getTransaction().commit();
                // Actualizar la sesión con el cliente modificado
                usuario.getCliente().setDireccion(nuevaDir.trim());
                out.print("{\"ok\":true}");
                return;

            } else if ("telefono".equals(tipo)) {
                if ("eliminar".equals(accion) && idStr != null) {
                    Telefonocliente t = em.find(Telefonocliente.class, Integer.parseInt(idStr));
                    if (t != null && t.getCliente().getIdCliente() == idCliente) {
                        t.setActivo(false);
                    }
                } else {
                    String numero    = request.getParameter("numero");
                    String tipoTelStr = request.getParameter("tipoTel");
                    if (numero == null || numero.isBlank()) {
                        em.getTransaction().rollback();
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print("{\"error\":\"El n\u00famero es obligatorio\"}");
                        return;
                    }
                    // Validar formato
                    String limpio = numero.replaceAll("[\\s\\-\\(\\)\\+]", "");
                    if (!limpio.matches("\\d{7,15}")) {
                        em.getTransaction().rollback();
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print("{\"error\":\"Formato de tel\u00e9fono inv\u00e1lido\"}");
                        return;
                    }
                    // Validar duplicado
                    TypedQuery<Long> dupQ = em.createQuery(
                        "SELECT COUNT(t) FROM Telefonocliente t WHERE t.cliente.idCliente = :id AND t.telefono = :num AND t.activo = true",
                        Long.class);
                    dupQ.setParameter("id", idCliente);
                    dupQ.setParameter("num", numero.trim());
                    if (dupQ.getSingleResult() > 0) {
                        em.getTransaction().rollback();
                        response.setStatus(HttpServletResponse.SC_CONFLICT);
                        out.print("{\"error\":\"Este n\u00famero ya est\u00e1 registrado\"}");
                        return;
                    }
                    TipoTelefono tipoTel = TipoTelefono.CELULAR;
                    try { tipoTel = TipoTelefono.valueOf(tipoTelStr); } catch (Exception ignored) {}

                    Telefonocliente t = new Telefonocliente();
                    t.setCliente(clienteRef);
                    t.setTelefono(numero.trim());
                    t.setTipoTelefono(tipoTel);
                    t.setActivo(true);
                    em.persist(t);
                }

            } else if ("correo".equals(tipo)) {
                if ("eliminar".equals(accion) && idStr != null) {
                    Correocliente c = em.find(Correocliente.class, Integer.parseInt(idStr));
                    if (c != null && c.getCliente().getIdCliente() == idCliente) {
                        if (c.isPrincipal()) {
                            em.getTransaction().rollback();
                            response.setStatus(HttpServletResponse.SC_CONFLICT);
                            out.print("{\"error\":\"No se puede eliminar el correo principal\"}");
                            return;
                        }
                        c.setActivo(false);
                    }
                } else {
                    String correo = request.getParameter("correo");
                    if (correo == null || !correo.matches("^[\\w.\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
                        em.getTransaction().rollback();
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print("{\"error\":\"Formato de correo inv\u00e1lido\"}");
                        return;
                    }
                    // Validar duplicado
                    TypedQuery<Long> dupQ = em.createQuery(
                        "SELECT COUNT(c) FROM Correocliente c WHERE c.cliente.idCliente = :id AND LOWER(c.correo) = LOWER(:correo) AND c.activo = true",
                        Long.class);
                    dupQ.setParameter("id", idCliente);
                    dupQ.setParameter("correo", correo.trim());
                    if (dupQ.getSingleResult() > 0) {
                        em.getTransaction().rollback();
                        response.setStatus(HttpServletResponse.SC_CONFLICT);
                        out.print("{\"error\":\"Este correo ya est\u00e1 registrado\"}");
                        return;
                    }
                    Correocliente c = new Correocliente();
                    c.setCliente(clienteRef);
                    c.setCorreo(correo.trim());
                    c.setPrincipal(false);
                    c.setActivo(true);
                    em.persist(c);
                }
            }

            em.getTransaction().commit();
            out.print("{\"ok\":true}");

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
