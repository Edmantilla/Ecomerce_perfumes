package servlets; // Este archivo pertenece al paquete "servlets"

import java.io.IOException; // Excepción para errores de entrada/salida
import java.io.PrintWriter; // Para escribir texto en la respuesta HTTP
import java.util.List; // Para manejar listas de entidades
import javax.persistence.EntityManager; // Maneja la conexión y operaciones con la BD
import javax.persistence.TypedQuery; // Para ejecutar consultas JPQL con tipo específico (importado pero no usado directamente)
import javax.servlet.ServletException; // Excepción propia de los servlets
import javax.servlet.annotation.WebServlet; // Anotación para registrar el servlet en una URL
import javax.servlet.http.HttpServlet; // Clase base de todos los servlets HTTP
import javax.servlet.http.HttpServletRequest; // Representa la petición que llega del cliente
import javax.servlet.http.HttpServletResponse; // Representa la respuesta que se envía al cliente
import logica.Telefonocliente; // Entidad JPA que representa un teléfono de un cliente
import logica.Correocliente; // Entidad JPA que representa un correo adicional de un cliente
import logica.Usuario; // Entidad JPA que representa un usuario del sistema
import persistencias.JpaProvider; // Singleton que provee el EntityManagerFactory
import persistencias.UsuarioJpaController; // Controlador JPA con los métodos CRUD del usuario

/**
 * SvUsuarios — Servlet de gestión de usuarios para el panel admin.
 * GET: lista todos los usuarios con su rol, datos del cliente (nombre, dirección,
 *      teléfonos, correos adicionales) y cantidad de pedidos (requiere VER_USUARIOS).
 * POST accion=desactivar: desactiva una cuenta de usuario (requiere EDITAR_USUARIOS).
 * POST accion=cambiarRol: asigna un nuevo rol a un usuario (requiere EDITAR_USUARIOS).
 */
@WebServlet(name = "SvUsuarios", urlPatterns = {"/SvUsuarios"}) // Registra este servlet en la URL /SvUsuarios
public class SvUsuarios extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8"); // La respuesta será JSON en UTF-8
        PrintWriter out = response.getWriter(); // Obtiene el escritor para enviar texto al cliente - respuesta que el usuario vera en el navegador

        EntityManager em = null; // Se declara fuera del try para poder cerrarlo en el finally - reservando un nombre y un espacio en la memorioa del programa
        try {
            // Verifica que el usuario de sesión tenga el permiso VER_USUARIOS
            if (!AuthHelper.tienePermiso(request, "VER_USUARIOS")) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN); // Responde 403 Forbidden
                out.print("{\"error\":\"Sin permiso: VER_USUARIOS\"}");
                return; // Corta la ejecución, no continúa
            }

            UsuarioJpaController ctrl = new UsuarioJpaController(); // Crea el controlador JPA de usuarios
            List<Usuario> usuarios = ctrl.findUsuarioEntities(); // Trae todos los usuarios de BD
            // EntityManager separado para consultas de teléfonos y correos de cada cliente
            em = JpaProvider.getEntityManagerFactory().createEntityManager();

            StringBuilder sb = new StringBuilder("["); // Inicia el JSON como array
            for (int i = 0; i < usuarios.size(); i++) {
                Usuario u = usuarios.get(i); // Obtiene el usuario actual de la lista
                if (i > 0) sb.append(","); // Agrega coma separadora entre elementos (no antes del primero)
                sb.append("{");
                sb.append("\"id\":").append(u.getIdUsuario()).append(","); // ID del usuario
                sb.append("\"correo\":\"").append(escapeJson(u.getCorreoUsuario())).append("\","); // Correo principal del usuario
                sb.append("\"activo\":").append(u.isActivo()).append(","); // Si la cuenta está activa o desactivada
                sb.append("\"rol\":\"").append(u.getRol() != null ? escapeJson(u.getRol().getNombreRol()) : "").append("\","); // Nombre del rol asignado
                sb.append("\"idRol\":").append(u.getRol() != null ? u.getRol().getIdRol() : 0).append(","); // ID del rol asignado
                // Si no tiene cliente asociado es un usuario admin del sistema
                sb.append("\"nombre\":\"").append(
                    u.getCliente() != null ? escapeJson(u.getCliente().getNombreCompleto()) : "Admin" // Admin puro no tiene cliente
                ).append("\",");
                sb.append("\"registro\":\"").append(u.getCreatedAt() != null ? u.getCreatedAt().toString() : "").append("\","); // Fecha de creación de la cuenta

                if (u.getCliente() != null) { // Si el usuario tiene cliente asociado (no es admin puro)
                    int idC = u.getCliente().getIdCliente(); // ID del cliente
                    String dir = u.getCliente().getDireccion(); // Dirección del cliente
                    sb.append("\"direccion\":\"").append(escapeJson(dir != null ? dir : "")).append("\",");

                    // Consultar los teléfonos activos del cliente (puede tener varios)
                    List<Telefonocliente> tels = em.createQuery(
                        "SELECT t FROM Telefonocliente t WHERE t.cliente.idCliente = :id AND t.activo = true ORDER BY t.idTelefono",
                        Telefonocliente.class).setParameter("id", idC).getResultList();
                    sb.append("\"telefonos\":["); // Inicia el array de teléfonos
                    for (int j = 0; j < tels.size(); j++) {
                        if (j > 0) sb.append(","); // Coma entre teléfonos
                        sb.append("{\"numero\":\"").append(escapeJson(tels.get(j).getTelefono())).append("\","); // Número del teléfono
                        sb.append("\"tipo\":\"").append(tels.get(j).getTipoTelefono() != null ? tels.get(j).getTipoTelefono().name() : "").append("\"}"); // Tipo (enum): CELULAR, FIJO, etc.
                    }
                    sb.append("],"); // Cierra el array de teléfonos

                    // Consultar los correos adicionales activos del cliente
                    List<Correocliente> correos = em.createQuery(
                        "SELECT c FROM Correocliente c WHERE c.cliente.idCliente = :id AND c.activo = true ORDER BY c.idCorreo",
                        Correocliente.class).setParameter("id", idC).getResultList();
                    sb.append("\"correosAdicionales\":["); // Inicia el array de correos adicionales
                    for (int j = 0; j < correos.size(); j++) {
                        if (j > 0) sb.append(","); // Coma entre correos
                        sb.append("{\"correo\":\"").append(escapeJson(correos.get(j).getCorreo())).append("\","); // Dirección del correo
                        sb.append("\"principal\":").append(correos.get(j).isPrincipal()).append("}"); // Si es el correo principal del cliente
                    }
                    sb.append("],"); // Cierra el array de correos adicionales

                    // Contar cuántos pedidos ha hecho este cliente
                    Long numPedidos = em.createQuery(
                        "SELECT COUNT(p) FROM Pedido p WHERE p.cliente.idCliente = :id", Long.class)
                        .setParameter("id", idC).getSingleResult();
                    sb.append("\"numPedidos\":").append(numPedidos); // Total de pedidos del cliente
                } else {
                    // Usuario sin cliente (admin puro): no tiene dirección, teléfonos ni pedidos
                    sb.append("\"direccion\":\"\",\"telefonos\":[],\"correosAdicionales\":[],\"numPedidos\":0");
                }

                sb.append("}"); // Cierra el objeto JSON del usuario
            }
            sb.append("]"); // Cierra el array JSON
            out.print(sb.toString()); // Envía el JSON completo al cliente

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // Responde 500 si ocurre cualquier error
            out.print("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        } finally {
            if (em != null && em.isOpen()) em.close(); // Siempre cierra el EntityManager para evitar leaks de conexión
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8"); // Fuerza UTF-8 en los parámetros del request (para acentos y caracteres especiales)
        response.setContentType("application/json;charset=UTF-8"); // La respuesta será JSON en UTF-8
        PrintWriter out = response.getWriter(); // Obtiene el escritor para enviar texto al cliente

        try {
            // Verifica que el usuario de sesión tenga el permiso EDITAR_USUARIOS
            if (!AuthHelper.tienePermiso(request, "EDITAR_USUARIOS")) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN); // Responde 403 Forbidden
                out.print("{\"error\":\"Sin permiso: EDITAR_USUARIOS\"}");
                return; // Corta la ejecución
            }

            String accion = request.getParameter("accion"); // Lee qué acción se solicitó: "desactivar" o "cambiarRol"
            if ("desactivar".equals(accion)) {
                // DESACTIVAR: marcar el usuario como inactivo (no se elimina físicamente de la BD)
                int id = Integer.parseInt(request.getParameter("id")); // ID del usuario a desactivar
                UsuarioJpaController ctrl = new UsuarioJpaController(); // Controlador JPA del usuario
                Usuario u = ctrl.findUsuario(id); // Busca el usuario en BD por su ID
                if (u != null) {
                    u.setActivo(false); // La cuenta queda deshabilitada para login
                    ctrl.edit(u); // Ejecuta el UPDATE en BD
                }
                out.print("{\"ok\":true}"); // Confirma al frontend que la operación fue exitosa
            } else if ("cambiarRol".equals(accion)) {
                // CAMBIAR ROL: asignar un rol diferente al usuario
                // Cambiar el rol cambia automáticamente los permisos que tiene el usuario
                int idUsuario = Integer.parseInt(request.getParameter("id")); // ID del usuario a modificar
                int idRol     = Integer.parseInt(request.getParameter("idRol")); // ID del nuevo rol a asignar
                EntityManager em = JpaProvider.getEntityManagerFactory().createEntityManager(); // Abre EntityManager con transacción manual
                try {
                    em.getTransaction().begin(); // Inicia la transacción
                    Usuario u = em.find(Usuario.class, idUsuario); // Busca el usuario por PK dentro de la transacción activa
                    if (u == null) { out.print("{\"error\":\"Usuario no encontrado\"}"); return; } // Si no existe, responde error
                    logica.Rol nuevoRol = em.find(logica.Rol.class, idRol); // Busca el Rol por PK
                    if (nuevoRol == null) { out.print("{\"error\":\"Rol no encontrado\"}"); return; } // Si el rol no existe, responde error
                    u.setRol(nuevoRol); // Asigna el nuevo rol al usuario
                    u.setUpdatedAt(java.time.LocalDateTime.now()); // Actualiza el timestamp de última modificación
                    em.merge(u); // Marca el usuario como modificado dentro de la transacción
                    em.getTransaction().commit(); // Confirma la transacción y genera el UPDATE en BD
                    // Retornar el nombre del nuevo rol para que el frontend actualice la UI sin recargar
                    out.print("{\"ok\":true,\"rol\":\"" + escapeJson(nuevoRol.getNombreRol()) + "\"}");
                } finally {
                    if (em.isOpen()) em.close(); // Cierra el EntityManager siempre, incluso si hubo error
                }
            } else {
                out.print("{\"error\":\"Acción desconocida\"}"); // La acción recibida no es ninguna de las esperadas
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // Responde 500 si ocurre cualquier error
            out.print("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
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
