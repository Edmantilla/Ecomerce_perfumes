package servlets; // Este archivo pertenece al paquete "servlets"

import enums.TipoTelefono; // Enum con los tipos de teléfono: CELULAR, FIJO, etc.
import java.io.IOException; // Excepción para errores de entrada/salida
import java.io.PrintWriter; // Para escribir texto en la respuesta HTTP
import java.util.List; // Para manejar listas de entidades
import javax.persistence.EntityManager; // Maneja la conexión y operaciones con la BD
import javax.persistence.TypedQuery; // Para ejecutar consultas JPQL con tipo específico
import javax.servlet.ServletException; // Excepción propia de los servlets
import javax.servlet.http.HttpServlet; // Clase base de todos los servlets HTTP
import javax.servlet.http.HttpServletRequest; // Representa la petición que llega del cliente
import javax.servlet.http.HttpServletResponse; // Representa la respuesta que se envía al cliente
import javax.servlet.http.HttpSession; // Representa la sesión HTTP del usuario logueado
import logica.Cliente; // Entidad JPA que representa un cliente
import logica.Correocliente; // Entidad JPA que representa un correo adicional del cliente
import logica.Telefonocliente; // Entidad JPA que representa un teléfono del cliente
import logica.Usuario; // Entidad JPA que representa un usuario del sistema
import persistencias.JpaProvider; // Singleton que provee el EntityManagerFactory

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
public class SvContactoCliente extends HttpServlet { // No usa @WebServlet, se registra en web.xml

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8"); // La respuesta será JSON en UTF-8
        PrintWriter out = response.getWriter(); // Obtiene el escritor para enviar texto al cliente

        // Verificar que haya sesión activa antes de retornar datos personales del cliente
        HttpSession session = request.getSession(false); // false = no crear sesión si no existe
        if (session == null || session.getAttribute("usuario") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // Responde 401 Unauthorized
            out.print("{\"error\":\"Sesión no iniciada\"}");
            return; // Corta la ejecución, no retorna datos sin sesión
        }

        EntityManager em = null; // Se declara fuera del try para poder cerrarlo en el finally
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario"); // Obtiene el usuario de la sesión
            Cliente cliente = usuario.getCliente(); // Obtiene el cliente asociado al usuario
            // Si el usuario no tiene cliente asociado (es admin puro) retornar listas vacías
            if (cliente == null) {
                out.print("{\"telefonos\":[],\"correos\":[]}"); // Admin no tiene teléfonos ni correos de cliente
                return;
            }
            int idCliente = cliente.getIdCliente(); // ID del cliente para las consultas
            em = JpaProvider.getEntityManagerFactory().createEntityManager(); // Abre la conexión con la BD

            // Consultar todos los teléfonos activos del cliente, ordenados por ID
            TypedQuery<Telefonocliente> qt = em.createQuery(
                "SELECT t FROM Telefonocliente t WHERE t.cliente.idCliente = :id AND t.activo = true ORDER BY t.idTelefono",
                Telefonocliente.class); // JPQL: filtra por cliente y solo los activos
            qt.setParameter("id", idCliente);
            List<Telefonocliente> tels = qt.getResultList(); // Lista de teléfonos activos del cliente

            // Consultar todos los correos adicionales activos del cliente
            TypedQuery<Correocliente> qc = em.createQuery(
                "SELECT c FROM Correocliente c WHERE c.cliente.idCliente = :id AND c.activo = true ORDER BY c.idCorreo",
                Correocliente.class); // JPQL: filtra por cliente y solo los activos
            qc.setParameter("id", idCliente);
            List<Correocliente> correos = qc.getResultList(); // Lista de correos adicionales activos

            StringBuilder sb = new StringBuilder("{");
            sb.append("\"telefonos\":["); // Inicia el array de teléfonos
            for (int i = 0; i < tels.size(); i++) {
                Telefonocliente t = tels.get(i); // Teléfono actual
                if (i > 0) sb.append(","); // Coma separadora entre elementos
                sb.append("{\"id\":").append(t.getIdTelefono()).append(","); // ID del teléfono (para eliminar)
                sb.append("\"numero\":\"").append(escapeJson(t.getTelefono())).append("\","); // Número de teléfono
                sb.append("\"tipo\":\"").append(t.getTipoTelefono() != null ? t.getTipoTelefono().name() : "").append("\"}"); // Tipo como string del enum
            }
            sb.append("],\"correos\":["); // Cierra teléfonos e inicia el array de correos
            for (int i = 0; i < correos.size(); i++) {
                Correocliente c = correos.get(i); // Correo actual
                if (i > 0) sb.append(","); // Coma separadora entre elementos
                sb.append("{\"id\":").append(c.getIdCorreo()).append(","); // ID del correo (para eliminar)
                sb.append("\"correo\":\"").append(escapeJson(c.getCorreo())).append("\","); // Dirección de correo
                sb.append("\"principal\":").append(c.isPrincipal()).append("}"); // Si es el correo principal del cliente
            }
            sb.append("]}"); // Cierra el array de correos y el objeto raíz
            out.print(sb.toString()); // Envía el JSON al cliente

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // Responde 500 si ocurre cualquier error
            out.print("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        } finally {
            if (em != null && em.isOpen()) em.close(); // Siempre cierra el EntityManager para evitar leaks
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8"); // Fuerza UTF-8 en los parámetros del request
        response.setContentType("application/json;charset=UTF-8"); // La respuesta será JSON en UTF-8
        PrintWriter out = response.getWriter(); // Obtiene el escritor para enviar texto al cliente

        // Verificar que haya sesión activa antes de realizar cualquier operación de escritura
        HttpSession session = request.getSession(false); // false = no crear sesión si no existe
        if (session == null || session.getAttribute("usuario") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // Responde 401 Unauthorized
            out.print("{\"error\":\"Sesión no iniciada\"}");
            return; // Corta la ejecución
        }

        EntityManager em = null; // Se declara fuera del try para poder cerrarlo en el finally
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario"); // Obtiene el usuario de la sesión
            Cliente cliente = usuario.getCliente(); // Obtiene el cliente asociado al usuario
            // Si el usuario no tiene cliente asociado (es admin puro) retornar error
            if (cliente == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"El usuario no tiene cliente asociado\"}");
                return;
            }
            int idCliente = cliente.getIdCliente(); // ID del cliente para las operaciones

            String tipo   = request.getParameter("tipo");   // Tipo de operación: "direccion" | "telefono" | "correo" | "cambiarContrasena"
            String accion = request.getParameter("accion"); // Acción específica: "agregar" | "eliminar"
            String idStr  = request.getParameter("id");     // ID del registro a eliminar (teléfono o correo)

            em = JpaProvider.getEntityManagerFactory().createEntityManager(); // Abre la conexión con la BD
            em.getTransaction().begin(); // Inicia la transacción (todas las operaciones se hacen en una sola transacción)

            // Recargar el cliente desde BD dentro de la transacción activa para que persist/merge funcionen correctamente
            Cliente clienteRef = em.find(Cliente.class, idCliente);

            if ("direccion".equals(tipo)) {
                // DIRECCIÓN: actualizar la dirección de entrega del cliente
                String nuevaDir = request.getParameter("direccion"); // Nueva dirección ingresada
                if (nuevaDir == null || nuevaDir.isBlank()) {
                    em.getTransaction().rollback(); // Revierte la transacción antes de retornar
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"La dirección no puede estar vacía\"}");
                    return;
                }
                clienteRef.setDireccion(nuevaDir.trim()); // UPDATE en BD: nueva dirección del cliente
                em.getTransaction().commit(); // Confirma el cambio de dirección
                // Sincronizar el objeto en sesión para que el frontend vea el cambio de inmediato sin recargar
                usuario.getCliente().setDireccion(nuevaDir.trim());
                out.print("{\"ok\":true}");
                return; // Sale del método

            } else if ("telefono".equals(tipo)) {
                if ("eliminar".equals(accion) && idStr != null) {
                    // ELIMINAR TELÉFONO: desactivar en vez de borrar físicamente (soft delete)
                    Telefonocliente t = em.find(Telefonocliente.class, Integer.parseInt(idStr)); // Busca el teléfono por ID
                    // Verificar que el teléfono pertenezca al cliente logueado (seguridad: evitar que un cliente elimine datos de otro)
                    if (t != null && t.getCliente().getIdCliente() == idCliente) {
                        t.setActivo(false); // Desactivar (soft delete: no se borra físicamente de la BD)
                    }
                } else {
                    // AGREGAR TELÉFONO: validar formato y duplicados antes de persistir
                    String numero     = request.getParameter("numero"); // Número de teléfono ingresado
                    String tipoTelStr = request.getParameter("tipoTel"); // Tipo: CELULAR, FIJO, etc.
                    if (numero == null || numero.isBlank()) {
                        em.getTransaction().rollback();
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print("{\"error\":\"El número es obligatorio\"}");
                        return;
                    }
                    // Validar formato: solo dígitos (7-15), eliminando espacios, guiones y paréntesis primero
                    String limpio = numero.replaceAll("[\\s\\-\\(\\)\\+]", ""); // Limpia caracteres de formato
                    if (!limpio.matches("\\d{7,15}")) { // Verifica que queden entre 7 y 15 dígitos
                        em.getTransaction().rollback();
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print("{\"error\":\"Formato de teléfono inválido\"}");
                        return;
                    }
                    // Verificar que el número no esté ya registrado activo para este cliente (sin duplicados)
                    TypedQuery<Long> dupQ = em.createQuery(
                        "SELECT COUNT(t) FROM Telefonocliente t WHERE t.cliente.idCliente = :id AND t.telefono = :num AND t.activo = true",
                        Long.class);
                    dupQ.setParameter("id", idCliente);
                    dupQ.setParameter("num", numero.trim());
                    if (dupQ.getSingleResult() > 0) {
                        em.getTransaction().rollback();
                        response.setStatus(HttpServletResponse.SC_CONFLICT); // HTTP 409 Conflict
                        out.print("{\"error\":\"Este número ya está registrado\"}");
                        return; // Número duplicado
                    }
                    // Convertir el tipo a enum; si no se reconoce, usar CELULAR por defecto
                    TipoTelefono tipoTel = TipoTelefono.CELULAR; // Valor por defecto
                    try { tipoTel = TipoTelefono.valueOf(tipoTelStr); } catch (Exception ignored) {} // Si el string no es un enum válido, usa CELULAR

                    Telefonocliente t = new Telefonocliente(); // Crea la entidad
                    t.setCliente(clienteRef); // Asocia el teléfono al cliente
                    t.setTelefono(numero.trim()); // Número de teléfono
                    t.setTipoTelefono(tipoTel); // Tipo de teléfono
                    t.setActivo(true); // Activo por defecto al crearlo
                    em.persist(t); // INSERT en BD
                }

            } else if ("cambiarContrasena".equals(tipo)) {
                // CAMBIAR CONTRASEÑA: valida la actual, aplica reglas y actualiza en BD
                String actual    = request.getParameter("actual");    // Contraseña actual del usuario
                String nueva     = request.getParameter("nueva");     // Nueva contraseña deseada
                String confirmar = request.getParameter("confirmar"); // Confirmación de la nueva contraseña

                // Validar que se ingresó la contraseña actual
                if (actual == null || actual.isBlank()) {
                    em.getTransaction().rollback();
                    out.print("{\"error\":\"Ingresa tu contrase\\u00f1a actual.\"}"); // \u00f1 = ñ
                    return;
                }
                // Recargar usuario desde BD dentro de la transacción para verificar contraseña real
                Usuario usuarioRef = em.find(Usuario.class, usuario.getIdUsuario());
                if (!usuarioRef.getContrasena().equals(actual)) { // Comparación en texto plano (diseño del proyecto)
                    em.getTransaction().rollback();
                    out.print("{\"error\":\"La contrase\\u00f1a actual es incorrecta.\"}");
                    return; // Contraseña actual incorrecta
                }
                // Validar que la nueva contraseña no esté vacía
                if (nueva == null || nueva.isBlank()) {
                    em.getTransaction().rollback();
                    out.print("{\"error\":\"Ingresa la nueva contrase\\u00f1a.\"}");
                    return;
                }
                // Validar longitud: entre 8 y 20 caracteres
                if (nueva.length() < 8 || nueva.length() > 20) {
                    em.getTransaction().rollback();
                    out.print("{\"error\":\"La contrase\\u00f1a debe tener entre 8 y 20 caracteres.\"}");
                    return;
                }
                // Validar que tenga al menos una letra y un número
                if (!nueva.matches(".*[a-zA-Z].*") || !nueva.matches(".*[0-9].*")) {
                    em.getTransaction().rollback();
                    out.print("{\"error\":\"La contrase\\u00f1a debe contener al menos una letra y un n\\u00famero.\"}"); // \u00fa = ú
                    return;
                }
                // Validar que la nueva contraseña y la confirmación coincidan
                if (!nueva.equals(confirmar)) {
                    em.getTransaction().rollback();
                    out.print("{\"error\":\"Las contrase\\u00f1as no coinciden.\"}");
                    return;
                }
                usuarioRef.setContrasena(nueva); // Actualiza la contraseña (texto plano, diseño del proyecto)
                usuarioRef.setUpdatedAt(java.time.LocalDateTime.now()); // Actualiza el timestamp de modificación
                em.merge(usuarioRef); // UPDATE en BD
                em.getTransaction().commit(); // Confirma el cambio de contraseña
                // Sincronizar la sesión para que las próximas validaciones usen la nueva contraseña
                usuario.setContrasena(nueva);
                out.print("{\"ok\":true}");
                return; // Sale del método

            } else if ("correo".equals(tipo)) {
                if ("eliminar".equals(accion) && idStr != null) {
                    // ELIMINAR CORREO: no se puede eliminar el correo principal del cliente
                    Correocliente c = em.find(Correocliente.class, Integer.parseInt(idStr)); // Busca el correo por ID
                    // Verificar que el correo pertenezca al cliente logueado (seguridad)
                    if (c != null && c.getCliente().getIdCliente() == idCliente) {
                        if (c.isPrincipal()) {
                            // El correo principal es necesario para el login; bloquearlo evita dejar la cuenta sin acceso
                            em.getTransaction().rollback();
                            response.setStatus(HttpServletResponse.SC_CONFLICT); // HTTP 409 Conflict
                            out.print("{\"error\":\"No se puede eliminar el correo principal\"}");
                            return;
                        }
                        c.setActivo(false); // Desactivar correo adicional (soft delete)
                    }
                } else {
                    // AGREGAR CORREO: validar formato y duplicados antes de persistir
                    String correo = request.getParameter("correo"); // Dirección de correo a agregar
                    // Validar formato con regex básico de correo electrónico (user@dominio.ext)
                    if (correo == null || !correo.matches("^[\\w.\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
                        em.getTransaction().rollback();
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print("{\"error\":\"Formato de correo inválido\"}");
                        return;
                    }
                    // Verificar que el correo no esté ya registrado activo para este cliente (sin duplicados)
                    TypedQuery<Long> dupQ = em.createQuery(
                        "SELECT COUNT(c) FROM Correocliente c WHERE c.cliente.idCliente = :id AND LOWER(c.correo) = LOWER(:correo) AND c.activo = true",
                        Long.class); // Insensible a mayúsculas para evitar duplicados con diferente capitalización
                    dupQ.setParameter("id", idCliente);
                    dupQ.setParameter("correo", correo.trim());
                    if (dupQ.getSingleResult() > 0) {
                        em.getTransaction().rollback();
                        response.setStatus(HttpServletResponse.SC_CONFLICT); // HTTP 409 Conflict
                        out.print("{\"error\":\"Este correo ya está registrado\"}");
                        return; // Correo duplicado
                    }
                    // Crear el nuevo correo adicional (nunca principal, el principal es el del usuario)
                    Correocliente c = new Correocliente(); // Crea la entidad
                    c.setCliente(clienteRef); // Asocia el correo al cliente
                    c.setCorreo(correo.trim()); // Dirección de correo
                    c.setPrincipal(false); // Solo se puede tener un correo principal (el del usuario en tabla usuario)
                    c.setActivo(true); // Activo por defecto al crearlo
                    em.persist(c); // INSERT en BD
                }
            }

            em.getTransaction().commit(); // Confirma todos los cambios de la transacción (teléfono o correo)
            out.print("{\"ok\":true}"); // Responde éxito al frontend

        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback(); // Si la transacción quedó abierta por el error, se revierte
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // Responde 500 Internal Server Error
            out.print("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
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
