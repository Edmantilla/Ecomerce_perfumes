package servlets; // Este archivo pertenece al paquete "servlets"

import java.io.IOException; // Excepción para errores de entrada/salida
import java.io.PrintWriter; // Para escribir texto en la respuesta HTTP
import java.util.List; // Para manejar listas de entidades
import javax.persistence.EntityManager; // Maneja la conexión y operaciones con la BD
import javax.persistence.TypedQuery; // Para ejecutar consultas JPQL con tipo específico y evitar casteos
import javax.servlet.ServletException; // Excepción propia de los servlets
import javax.servlet.annotation.WebServlet; // Anotación para registrar el servlet en una URL
import javax.servlet.http.HttpServlet; // Clase base de todos los servlets HTTP
import javax.servlet.http.HttpServletRequest; // Representa la petición que llega del cliente
import javax.servlet.http.HttpServletResponse; // Representa la respuesta que se envía al cliente
import logica.Usuario; // Entidad JPA que representa un usuario del sistema
import persistencias.JpaProvider; // Singleton que provee el EntityManagerFactory

/**
 * SvRecuperarContrasena — Servlet de recuperación de contraseña sin envío de correo.
 * Implementa un flujo de 2 pasos vía AJAX desde olvide_contrasena.jsp:
 *   Paso 1 — POST accion=verificar: comprueba que el correo existe en BD y retorna el nombre del usuario.
 *   Paso 2 — POST accion=cambiar: valida la nueva contraseña y la actualiza directamente en BD.
 * GET: redirige a la página de olvide_contrasena.jsp.
 */
@WebServlet(name = "SvRecuperarContrasena", urlPatterns = {"/SvRecuperarContrasena"}) // Registra este servlet en la URL /SvRecuperarContrasena
public class SvRecuperarContrasena extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8"); // Fuerza UTF-8 en los parámetros del request (para acentos y caracteres especiales)
        response.setContentType("application/json;charset=UTF-8"); // La respuesta será JSON en UTF-8
        PrintWriter out = response.getWriter(); // Obtiene el escritor para enviar texto al cliente

        // Determinar la acción: "verificar" (paso 1) o "cambiar" (paso 2)
        String accion = request.getParameter("accion"); // Lee qué acción solicitó el frontend
        String correo = request.getParameter("correo"); // Correo del usuario que quiere recuperar su cuenta

        // Validación: el correo es obligatorio para ambos pasos
        if (correo == null || correo.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // Responde 400 Bad Request
            out.print("{\"error\":\"El correo es obligatorio\"}");
            return; // Corta la ejecución, no continúa
        }

        EntityManager em = null; // Se declara fuera del try para poder cerrarlo en el finally
        try {
            em = JpaProvider.getEntityManagerFactory().createEntityManager(); // Abre la conexión con la BD

            // Buscar un usuario activo con ese correo (búsqueda insensible a mayúsculas con LOWER)
            TypedQuery<Usuario> q = em.createQuery(
                "SELECT u FROM Usuario u WHERE LOWER(u.correoUsuario) = LOWER(:c) AND u.activo = true",
                Usuario.class);
            q.setParameter("c", correo.trim()); // trim() elimina espacios accidentales al inicio/fin
            List<Usuario> res = q.getResultList(); // Ejecuta la consulta y obtiene los resultados

            // Si no existe ninguna cuenta activa con ese correo, retornar error amigable
            if (res.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND); // Responde 404 Not Found
                out.print("{\"error\":\"No existe una cuenta activa con ese correo\"}");
                return; // Corta la ejecución
            }

            Usuario usuario = res.get(0); // Toma el primer (y único) usuario encontrado

            if ("verificar".equals(accion)) {
                // PASO 1: confirmar que el correo existe y retornar el nombre del usuario
                // El frontend usa esto para mostrar "Bienvenido, [nombre]" antes del paso 2
                String nombre = (usuario.getCliente() != null)
                    ? usuario.getCliente().getNombreCompleto() // Si tiene cliente, usa su nombre completo
                    : "Administrador"; // Si no tiene cliente, es un admin del sistema
                out.print("{\"ok\":true,\"nombre\":\"" + esc(nombre) + "\"}"); // Responde con el nombre para que el frontend lo muestre

            } else if ("cambiar".equals(accion)) {
                // PASO 2: validar y actualizar la nueva contraseña
                String nueva     = request.getParameter("nueva");     // Nueva contraseña deseada
                String confirmar = request.getParameter("confirmar"); // Confirmación de la contraseña

                // Validación: el campo nueva contraseña no puede estar vacío
                if (nueva == null || nueva.isBlank()) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // Responde 400 Bad Request
                    out.print("{\"error\":\"La nueva contrase\\u00f1a es obligatoria\"}"); // \u00f1 = ñ en unicode
                    return;
                }
                // Validación: la contraseña debe tener entre 8 y 20 caracteres
                if (nueva.length() < 8 || nueva.length() > 20) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"La contrase\\u00f1a debe tener entre 8 y 20 caracteres\"}");
                    return;
                }
                // Validación: la contraseña debe tener al menos 1 letra y 1 número
                if (!nueva.matches(".*[a-zA-Z].*") || !nueva.matches(".*[0-9].*")) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"La contrase\\u00f1a debe contener al menos una letra y un n\\u00famero\"}"); // \u00fa = ú en unicode
                    return;
                }
                // Validación: las dos contraseñas ingresadas deben coincidir exactamente
                if (!nueva.equals(confirmar)) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"Las contrase\\u00f1as no coinciden\"}");
                    return;
                }

                // Todas las validaciones pasaron: actualizar la contraseña en BD (texto plano, según las convenciones del proyecto)
                em.getTransaction().begin(); // Inicia la transacción
                Usuario u = em.find(Usuario.class, usuario.getIdUsuario()); // Recarga el usuario dentro de la transacción para que merge funcione correctamente
                u.setContrasena(nueva); // Asigna la nueva contraseña
                u.setUpdatedAt(java.time.LocalDateTime.now()); // Actualiza el timestamp de última modificación
                em.merge(u); // Marca el usuario como modificado dentro de la transacción
                em.getTransaction().commit(); // Confirma la transacción y genera el UPDATE en BD

                out.print("{\"ok\":true}"); // El frontend muestra la pantalla de éxito al recibir esto

            } else {
                // La acción recibida no es "verificar" ni "cambiar"
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Acci\\u00f3n desconocida\"}"); // \u00f3 = ó en unicode
            }

        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback(); // Si la transacción quedó abierta por el error, se revierte
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // Responde 500 Internal Server Error
            out.print("{\"error\":\"" + esc(e.getMessage()) + "\"}");
        } finally {
            if (em != null && em.isOpen()) em.close(); // Siempre cierra el EntityManager para evitar leaks de conexión
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Si alguien navega directamente a /SvRecuperarContrasena con GET, lo redirige a la página del formulario
        response.sendRedirect(request.getContextPath() + "/vistas/olvide_contrasena.jsp");
    }

    // Escapa caracteres especiales para que no rompan el JSON de salida
    private String esc(String s) {
        if (s == null) return ""; // Si el string es null retorna vacío para no generar NullPointerException
        return s.replace("\\", "\\\\") // Escapa backslashes
                .replace("\"", "\\\"") // Escapa comillas dobles
                .replace("\n", "\\n").replace("\r", "\\r"); // Escapa saltos de línea
    }
}
