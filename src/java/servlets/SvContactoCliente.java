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

/**
 * SvContactoCliente — Servlet de gestión de datos de contacto del cliente logueado.
 * GET: retorna todos los teléfonos y correos adicionales activos del cliente en sesión.
 * POST tipo=direccion: actualiza la dirección de entrega del cliente.
 * POST tipo=telefono accion=agregar: agrega un nuevo teléfono (valida formato y duplicados).
 * POST tipo=telefono accion=eliminar: desactiva un teléfono existente (no se elimina físicamente).
 * POST tipo=correo accion=agregar: agrega un nuevo correo adicional (valida formato y duplicados).
 * POST tipo=correo accion=eliminar: desactiva un correo adicional. Bloqueado si es el correo principal.
 * Requiere sesión activa en todas las operaciones.
 */
public class SvContactoCliente extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        // Verificar que haya sesión activa antes de retornar datos personales
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"error\":\"Sesión no iniciada\"}");
            return;
        }

        EntityManager em = null;
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            Cliente cliente = usuario.getCliente();
            // Si el usuario no tiene cliente asociado (es admin puro) retornar listas vacías
            if (cliente == null) {
                out.print("{\"telefonos\":[],\"correos\":[]}");
                return;
            }
            int idCliente = cliente.getIdCliente();
            em = JpaProvider.getEntityManagerFactory().createEntityManager();

            // Consultar todos los teléfonos activos del cliente, ordenados por ID
            TypedQuery<Telefonocliente> qt = em.createQuery(
                "SELECT t FROM Telefonocliente t WHERE t.cliente.idCliente = :id AND t.activo = true ORDER BY t.idTelefono",
                Telefonocliente.class);
            qt.setParameter("id", idCliente);
            List<Telefonocliente> tels = qt.getResultList();

            // Consultar todos los correos adicionales activos del cliente
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

        // Verificar que haya sesión activa antes de realizar operaciones
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"error\":\"Sesión no iniciada\"}");
            return;
        }

        EntityManager em = null;
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            Cliente cliente = usuario.getCliente();
            // Si el usuario no tiene cliente asociado (es admin puro) retornar error
            if (cliente == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"El usuario no tiene cliente asociado\"}");
                return;
            }
            int idCliente = cliente.getIdCliente();

            String tipo   = request.getParameter("tipo");   // "direccion" | "telefono" | "correo"
            String accion = request.getParameter("accion"); // "agregar" | "eliminar"
            String idStr  = request.getParameter("id");     // ID del registro a eliminar

            em = JpaProvider.getEntityManagerFactory().createEntityManager();
            em.getTransaction().begin();

            // Recargar el cliente desde BD dentro de la transacción activa
            Cliente clienteRef = em.find(Cliente.class, idCliente);

            if ("direccion".equals(tipo)) {
                // DIRECCIÓN: actualizar la dirección de entrega del cliente
                String nuevaDir = request.getParameter("direccion");
                if (nuevaDir == null || nuevaDir.isBlank()) {
                    em.getTransaction().rollback();
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"La dirección no puede estar vacía\"}");
                    return;
                }
                clienteRef.setDireccion(nuevaDir.trim()); // UPDATE en BD
                em.getTransaction().commit();
                // Sincronizar el objeto en sesión para que el frontend vea el cambio de inmediato
                usuario.getCliente().setDireccion(nuevaDir.trim());
                out.print("{\"ok\":true}");
                return;

            } else if ("telefono".equals(tipo)) {
                if ("eliminar".equals(accion) && idStr != null) {
                    // ELIMINAR TELÉFONO: desactivar en vez de borrar físicamente
                    Telefonocliente t = em.find(Telefonocliente.class, Integer.parseInt(idStr));
                    // Verificar que el teléfono pertenezca al cliente logueado (seguridad)
                    if (t != null && t.getCliente().getIdCliente() == idCliente) {
                        t.setActivo(false); // desactivar (soft delete)
                    }
                } else {
                    // AGREGAR TELÉFONO: validar formato y duplicados antes de persistir
                    String numero     = request.getParameter("numero");
                    String tipoTelStr = request.getParameter("tipoTel"); // CELULAR, FIJO, etc.
                    if (numero == null || numero.isBlank()) {
                        em.getTransaction().rollback();
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print("{\"error\":\"El número es obligatorio\"}");
                        return;
                    }
                    // Validar formato: solo dígitos (7-15), eliminando espacios, guíones y paréntesis
                    String limpio = numero.replaceAll("[\\s\\-\\(\\)\\+]", "");
                    if (!limpio.matches("\\d{7,15}")) {
                        em.getTransaction().rollback();
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print("{\"error\":\"Formato de teléfono inválido\"}");
                        return;
                    }
                    // Verificar que el número no esté ya registrado para este cliente
                    TypedQuery<Long> dupQ = em.createQuery(
                        "SELECT COUNT(t) FROM Telefonocliente t WHERE t.cliente.idCliente = :id AND t.telefono = :num AND t.activo = true",
                        Long.class);
                    dupQ.setParameter("id", idCliente);
                    dupQ.setParameter("num", numero.trim());
                    if (dupQ.getSingleResult() > 0) {
                        em.getTransaction().rollback();
                        response.setStatus(HttpServletResponse.SC_CONFLICT);
                        out.print("{\"error\":\"Este número ya está registrado\"}");
                        return;
                    }
                    // Convertir el tipo a enum; si no se reconoce, usar CELULAR por defecto
                    TipoTelefono tipoTel = TipoTelefono.CELULAR;
                    try { tipoTel = TipoTelefono.valueOf(tipoTelStr); } catch (Exception ignored) {}

                    Telefonocliente t = new Telefonocliente();
                    t.setCliente(clienteRef);
                    t.setTelefono(numero.trim());
                    t.setTipoTelefono(tipoTel);
                    t.setActivo(true);
                    em.persist(t); // INSERT en BD
                }

            } else if ("correo".equals(tipo)) {
                if ("eliminar".equals(accion) && idStr != null) {
                    // ELIMINAR CORREO: no se puede eliminar el correo principal del cliente
                    Correocliente c = em.find(Correocliente.class, Integer.parseInt(idStr));
                    // Verificar que el correo pertenezca al cliente logueado (seguridad)
                    if (c != null && c.getCliente().getIdCliente() == idCliente) {
                        if (c.isPrincipal()) {
                            // El correo principal es necesario para el login; no se puede eliminar
                            em.getTransaction().rollback();
                            response.setStatus(HttpServletResponse.SC_CONFLICT);
                            out.print("{\"error\":\"No se puede eliminar el correo principal\"}");
                            return;
                        }
                        c.setActivo(false); // desactivar correo adicional (soft delete)
                    }
                } else {
                    // AGREGAR CORREO: validar formato y duplicados
                    String correo = request.getParameter("correo");
                    // Validar formato con regex básico de correo electrónico
                    if (correo == null || !correo.matches("^[\\w.\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
                        em.getTransaction().rollback();
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print("{\"error\":\"Formato de correo inválido\"}");
                        return;
                    }
                    // Verificar que el correo no esté ya registrado para este cliente
                    TypedQuery<Long> dupQ = em.createQuery(
                        "SELECT COUNT(c) FROM Correocliente c WHERE c.cliente.idCliente = :id AND LOWER(c.correo) = LOWER(:correo) AND c.activo = true",
                        Long.class);
                    dupQ.setParameter("id", idCliente);
                    dupQ.setParameter("correo", correo.trim());
                    if (dupQ.getSingleResult() > 0) {
                        em.getTransaction().rollback();
                        response.setStatus(HttpServletResponse.SC_CONFLICT);
                        out.print("{\"error\":\"Este correo ya está registrado\"}");
                        return;
                    }
                    // Crear el nuevo correo adicional (no principal)
                    Correocliente c = new Correocliente();
                    c.setCliente(clienteRef);
                    c.setCorreo(correo.trim());
                    c.setPrincipal(false); // solo se puede tener un correo principal (el del usuario)
                    c.setActivo(true);
                    em.persist(c); // INSERT en BD
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
