package servlets; // Este archivo pertenece al paquete "servlets"

import java.io.IOException; // Excepción para errores de entrada/salida
import java.io.PrintWriter; // Para escribir texto en la respuesta HTTP
import java.time.LocalDateTime; // Para manejar fechas y horas (importado pero no usado directamente aquí)
import java.util.List; // Para manejar listas de entidades
import javax.persistence.EntityManager; // Maneja la conexión y operaciones con la BD
import javax.persistence.TypedQuery; // Para ejecutar consultas JPQL con tipo específico
import javax.servlet.ServletException; // Excepción propia de los servlets
import javax.servlet.annotation.WebServlet; // Anotación para registrar el servlet en una URL
import javax.servlet.http.HttpServlet; // Clase base de todos los servlets HTTP
import javax.servlet.http.HttpServletRequest; // Representa la petición que llega del cliente
import javax.servlet.http.HttpServletResponse; // Representa la respuesta que se envía al cliente
import logica.Permiso; // Entidad JPA que representa un permiso del sistema
import logica.Rol; // Entidad JPA que representa un rol (ej: VENDEDOR, SUPERVISOR)
import logica.Rolpermiso; // Entidad JPA que representa la asignación de un permiso a un rol (tabla intermedia)
import persistencias.JpaProvider; // Singleton que provee el EntityManagerFactory
import persistencias.PermisoJpaController; // Controlador JPA con CRUD de permisos
import persistencias.RolJpaController; // Controlador JPA con CRUD de roles
import persistencias.RolpermisoJpaController; // Controlador JPA con CRUD de asignaciones rol-permiso

/**
 * SvPermisos — Servlet de gestión de roles y permisos del sistema.
 * Requiere el permiso GESTIONAR_ROLES en todas las operaciones.
 *
 * GET /SvPermisos                  → lista todos los roles con sus permisos asignados
 * GET /SvPermisos?recurso=permisos → lista todos los permisos disponibles
 *
 * POST acciones disponibles:
 *   crearPermiso  — crea un nuevo permiso (nombre, descripcion, modulo)
 *   editarPermiso — edita un permiso existente (id, nombre, descripcion, modulo)
 *   togglePermiso — activa o desactiva un permiso (id)
 *   crearRol      — crea un nuevo rol (nombre, descripcion)
 *   editarRol     — edita un rol existente (id, nombre, descripcion)
 *   toggleRol     — activa o desactiva un rol; bloqueado si tiene usuarios activos asignados
 *   asignar       — asigna un permiso a un rol (idRol, idPermiso); evita duplicados
 *   revocar       — elimina la asignación rol-permiso (idRolPermiso)
 */
@WebServlet(name = "SvPermisos", urlPatterns = {"/SvPermisos"}) // Registra este servlet en la URL /SvPermisos
public class SvPermisos extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8"); // La respuesta será JSON en UTF-8
        PrintWriter out = response.getWriter(); // Obtiene el escritor para enviar texto al cliente

        // Verificar que el usuario tenga el permiso GESTIONAR_ROLES antes de cualquier operación
        if (!AuthHelper.tienePermiso(request, "GESTIONAR_ROLES")) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN); // Responde 403 Forbidden
            out.print("{\"error\":\"Sin permiso: GESTIONAR_ROLES\"}");
            return; // Corta la ejecución
        }

        // Determinar qué recurso se pide: "permisos" lista todos los permisos; por defecto lista roles
        String recurso = request.getParameter("recurso"); // Parámetro opcional de la URL
        EntityManager em = null; // Se declara fuera del try para poder cerrarlo en el finally
        try {
            em = JpaProvider.getEntityManagerFactory().createEntityManager(); // Abre la conexión con la BD

            if ("permisos".equals(recurso)) {
                // Retornar la lista plana de todos los permisos del sistema
                List<Permiso> permisos = new PermisoJpaController().findPermisoEntities(); // SELECT * FROM permiso
                StringBuilder sb = new StringBuilder("["); // Inicia el array JSON
                for (int i = 0; i < permisos.size(); i++) {
                    Permiso p = permisos.get(i); // Permiso actual
                    if (i > 0) sb.append(","); // Coma separadora entre elementos
                    sb.append("{");
                    sb.append("\"id\":").append(p.getIdPermiso()).append(","); // ID del permiso
                    sb.append("\"nombre\":\"").append(escapeJson(p.getNombrePermiso())).append("\","); // Nombre (ej: "VER_PEDIDOS")
                    sb.append("\"descripcion\":\"").append(escapeJson(p.getDescripcion())).append("\","); // Descripción legible
                    sb.append("\"modulo\":\"").append(escapeJson(p.getModulo())).append("\","); // Módulo al que pertenece (ej: "PEDIDOS")
                    sb.append("\"activo\":").append(p.isActivo()); // Si el permiso está activo o desactivado
                    sb.append("}");
                }
                sb.append("]"); // Cierra el array JSON
                out.print(sb.toString()); // Envía el JSON al cliente
                return; // Sale del método, no continúa al bloque de roles
            }

            // Por defecto: retornar roles con sus permisos anidados
            // El panel admin usa este endpoint para mostrar la tabla de roles y asignaciones
            List<Rol> roles = new RolJpaController().findRolEntities(); // SELECT * FROM rol
            StringBuilder sb = new StringBuilder("["); // Inicia el array JSON de roles
            for (int i = 0; i < roles.size(); i++) {
                Rol rol = roles.get(i); // Rol actual
                if (i > 0) sb.append(","); // Coma separadora entre roles

                // Consultar los permisos asignados a este rol via la tabla rolpermiso
                TypedQuery<Rolpermiso> qrp = em.createQuery(
                    "SELECT rp FROM Rolpermiso rp WHERE rp.rol.idRol = :id", Rolpermiso.class); // JOIN con la tabla intermedia
                qrp.setParameter("id", rol.getIdRol()); // Filtra por el ID del rol actual
                List<Rolpermiso> rps = qrp.getResultList(); // Lista de asignaciones rol-permiso

                sb.append("{");
                sb.append("\"id\":").append(rol.getIdRol()).append(","); // ID del rol
                sb.append("\"nombre\":\"").append(escapeJson(rol.getNombreRol())).append("\","); // Nombre del rol (ej: "VENDEDOR")
                sb.append("\"descripcion\":\"").append(escapeJson(rol.getDescripcion())).append("\","); // Descripción del rol
                sb.append("\"activo\":").append(rol.isActivo()).append(","); // Si el rol está activo
                // Anidar los permisos dentro del objeto del rol
                sb.append("\"permisos\":[");
                for (int j = 0; j < rps.size(); j++) {
                    Rolpermiso rp = rps.get(j); // Asignación rol-permiso actual
                    Permiso p = rp.getPermiso(); // Permiso asociado a esta asignación
                    if (p == null) continue; // Ignorar asignaciones con permiso nulo (dato corrupto)
                    if (j > 0) sb.append(","); // Coma separadora entre permisos
                    sb.append("{");
                    sb.append("\"idRolPermiso\":").append(rp.getIdRolPermiso()).append(","); // ID de la asignación (necesario para revocar)
                    sb.append("\"idPermiso\":").append(p.getIdPermiso()).append(","); // ID del permiso
                    sb.append("\"nombre\":\"").append(escapeJson(p.getNombrePermiso())).append("\","); // Nombre del permiso
                    sb.append("\"modulo\":\"").append(escapeJson(p.getModulo())).append("\","); // Módulo del permiso
                    sb.append("\"activo\":").append(p.isActivo()); // Si el permiso está activo
                    sb.append("}");
                }
                sb.append("]}"); // Cierra el array de permisos y el objeto del rol
            }
            sb.append("]"); // Cierra el array JSON de roles
            out.print(sb.toString()); // Envía el JSON al cliente

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // Responde 500 si ocurre cualquier error
            out.print("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        } finally {
            if (em != null && em.isOpen()) em.close(); // Siempre cierra el EntityManager para evitar leaks de conexión
        }
    }

    /**
     * POST acciones:
     *   accion=crearPermiso  nombre, descripcion, modulo
     *   accion=editarPermiso id, nombre, descripcion, modulo
     *   accion=togglePermiso id
     *   accion=asignar       idRol, idPermiso
     *   accion=revocar       idRolPermiso
     *   accion=crearRol      nombre, descripcion
     *   accion=editarRol     id, nombre, descripcion
     *   accion=toggleRol     id
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8"); // Fuerza UTF-8 en los parámetros del request
        response.setContentType("application/json;charset=UTF-8"); // La respuesta será JSON en UTF-8
        PrintWriter out = response.getWriter(); // Obtiene el escritor para enviar texto al cliente

        // Verificar permiso antes de cualquier operación de escritura
        if (!AuthHelper.tienePermiso(request, "GESTIONAR_ROLES")) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN); // Responde 403 Forbidden
            out.print("{\"error\":\"Sin permiso: GESTIONAR_ROLES\"}");
            return; // Corta la ejecución
        }

        EntityManager em = null; // Se declara fuera del try para poder cerrarlo en el finally
        try {
            String accion = request.getParameter("accion"); // Lee qué acción se solicitó
            em = JpaProvider.getEntityManagerFactory().createEntityManager(); // Abre la conexión con la BD

            switch (accion != null ? accion : "") { // Evalúa la acción; si es null usa "" para caer en default

                case "crearPermiso": {
                    // Crear un nuevo permiso del sistema (ej: "VER_REPORTES", "EXPORTAR_DATOS")
                    String nombre = request.getParameter("nombre"); // Nombre del nuevo permiso
                    if (nombre == null || nombre.isBlank()) {
                        bad(response, out, "El nombre es obligatorio"); return; // Valida campo obligatorio
                    }
                    // Verificar que no exista otro permiso con el mismo nombre (insensible a mayúsculas)
                    TypedQuery<Long> dup = em.createQuery(
                        "SELECT COUNT(p) FROM Permiso p WHERE LOWER(p.nombrePermiso) = LOWER(:n)", Long.class);
                    dup.setParameter("n", nombre.trim());
                    if (dup.getSingleResult() > 0) {
                        bad(response, out, "Ya existe un permiso con ese nombre"); return; // Nombre duplicado
                    }
                    Permiso p = new Permiso(); // Crea el objeto entidad
                    p.setNombrePermiso(nombre.trim()); // Nombre en mayúsculas por convención (ej: "VER_PEDIDOS")
                    p.setDescripcion(request.getParameter("descripcion")); // Descripción opcional
                    p.setModulo(request.getParameter("modulo")); // Módulo al que pertenece (ej: "PRODUCTOS")
                    p.setActivo(true); // Los permisos nuevos se crean activos
                    new PermisoJpaController().create(p); // INSERT en BD
                    out.print("{\"ok\":true}");
                    break;
                }

                case "editarPermiso": {
                    // Actualizar los datos de un permiso existente
                    int id = Integer.parseInt(request.getParameter("id")); // ID del permiso a editar
                    String nombre = request.getParameter("nombre"); // Nuevo nombre
                    if (nombre == null || nombre.isBlank()) {
                        bad(response, out, "El nombre es obligatorio"); return;
                    }
                    PermisoJpaController ctrl = new PermisoJpaController();
                    Permiso p = ctrl.findPermiso(id); // Busca el permiso en BD por su ID
                    if (p == null) { bad(response, out, "Permiso no encontrado"); return; } // No existe
                    p.setNombrePermiso(nombre.trim()); // Actualiza el nombre
                    p.setDescripcion(request.getParameter("descripcion")); // Actualiza la descripción
                    p.setModulo(request.getParameter("modulo")); // Actualiza el módulo
                    ctrl.edit(p); // UPDATE en BD
                    out.print("{\"ok\":true}");
                    break;
                }

                case "togglePermiso": {
                    // Activar o desactivar un permiso (toggle)
                    // Los permisos inactivos no son evaluados por AuthHelper.tienePermiso() aunque estén asignados al rol
                    int id = Integer.parseInt(request.getParameter("id")); // ID del permiso a toglear
                    PermisoJpaController ctrl = new PermisoJpaController();
                    Permiso p = ctrl.findPermiso(id); // Busca el permiso en BD
                    if (p == null) { bad(response, out, "Permiso no encontrado"); return; }
                    p.setActivo(!p.isActivo()); // Invierte el estado: activo→inactivo o inactivo→activo
                    ctrl.edit(p); // UPDATE en BD
                    out.print("{\"ok\":true}");
                    break;
                }

                case "crearRol": {
                    // Crear un nuevo rol (ej: "SUPERVISOR", "VENDEDOR")
                    String nombre = request.getParameter("nombre"); // Nombre del nuevo rol
                    if (nombre == null || nombre.isBlank()) {
                        bad(response, out, "El nombre es obligatorio"); return;
                    }
                    // Verificar que no exista otro rol con el mismo nombre
                    TypedQuery<Long> dup = em.createQuery(
                        "SELECT COUNT(r) FROM Rol r WHERE LOWER(r.nombreRol) = LOWER(:n)", Long.class);
                    dup.setParameter("n", nombre.trim());
                    if (dup.getSingleResult() > 0) {
                        bad(response, out, "Ya existe un rol con ese nombre"); return; // Nombre duplicado
                    }
                    Rol rol = new Rol(); // Crea el objeto entidad
                    rol.setNombreRol(nombre.trim()); // Nombre del rol
                    rol.setDescripcion(request.getParameter("descripcion")); // Descripción opcional
                    rol.setActivo(true); // Los roles nuevos se crean activos
                    new RolJpaController().create(rol); // INSERT en BD
                    out.print("{\"ok\":true}");
                    break;
                }

                case "editarRol": {
                    // Actualizar el nombre y descripción de un rol existente
                    int id = Integer.parseInt(request.getParameter("id")); // ID del rol a editar
                    String nombre = request.getParameter("nombre"); // Nuevo nombre
                    if (nombre == null || nombre.isBlank()) {
                        bad(response, out, "El nombre es obligatorio"); return;
                    }
                    RolJpaController ctrl = new RolJpaController();
                    Rol rol = ctrl.findRol(id); // Busca el rol en BD por su ID
                    if (rol == null) { bad(response, out, "Rol no encontrado"); return; }
                    rol.setNombreRol(nombre.trim()); // Actualiza el nombre
                    rol.setDescripcion(request.getParameter("descripcion")); // Actualiza la descripción
                    ctrl.edit(rol); // UPDATE en BD
                    out.print("{\"ok\":true}");
                    break;
                }

                case "toggleRol": {
                    // Activar o desactivar un rol
                    // Bloqueado si el rol tiene usuarios activos asignados (desactivarlo los dejaría sin acceso)
                    int id = Integer.parseInt(request.getParameter("id")); // ID del rol a toglear
                    RolJpaController ctrl = new RolJpaController();
                    Rol rol = ctrl.findRol(id); // Busca el rol en BD
                    if (rol == null) { bad(response, out, "Rol no encontrado"); return; }
                    if (rol.isActivo()) {
                        // Solo bloquear si se intenta DESACTIVAR un rol con usuarios activos asignados
                        TypedQuery<Long> q = em.createQuery(
                            "SELECT COUNT(u) FROM Usuario u WHERE u.rol.idRol = :id AND u.activo = true", Long.class);
                        q.setParameter("id", id); // Filtra por este rol
                        if (q.getSingleResult() > 0) {
                            bad(response, out, "No se puede desactivar: tiene usuarios activos asignados"); return; // Protección
                        }
                    }
                    rol.setActivo(!rol.isActivo()); // Invierte el estado del rol
                    ctrl.edit(rol); // UPDATE en BD
                    out.print("{\"ok\":true}");
                    break;
                }

                case "asignar": {
                    // Asignar un permiso a un rol (crea una fila en la tabla rolpermiso)
                    int idRol     = Integer.parseInt(request.getParameter("idRol")); // ID del rol destino
                    int idPermiso = Integer.parseInt(request.getParameter("idPermiso")); // ID del permiso a asignar
                    // Verificar que no esté ya asignado para evitar duplicados en la tabla
                    TypedQuery<Long> dup = em.createQuery(
                        "SELECT COUNT(rp) FROM Rolpermiso rp WHERE rp.rol.idRol = :r AND rp.permiso.idPermiso = :p",
                        Long.class);
                    dup.setParameter("r", idRol).setParameter("p", idPermiso);
                    if (dup.getSingleResult() > 0) {
                        bad(response, out, "El permiso ya está asignado a este rol"); return; // Ya existe la asignación
                    }
                    RolJpaController rolCtrl = new RolJpaController();
                    PermisoJpaController perCtrl = new PermisoJpaController();
                    Rol rol = rolCtrl.findRol(idRol); // Busca el rol en BD
                    Permiso permiso = perCtrl.findPermiso(idPermiso); // Busca el permiso en BD
                    if (rol == null || permiso == null) {
                        bad(response, out, "Rol o permiso no encontrado"); return; // Alguno no existe
                    }
                    Rolpermiso rp = new Rolpermiso(rol, permiso); // Crea la entidad de asignación
                    new RolpermisoJpaController().create(rp); // INSERT en la tabla rolpermiso
                    out.print("{\"ok\":true}");
                    break;
                }

                case "revocar": {
                    // Revocar un permiso de un rol: elimina la fila en rolpermiso por su ID
                    int idRolPermiso = Integer.parseInt(request.getParameter("idRolPermiso")); // ID de la asignación a eliminar
                    new RolpermisoJpaController().destroy(idRolPermiso); // DELETE de la asignación en BD
                    out.print("{\"ok\":true}");
                    break;
                }

                default:
                    bad(response, out, "Acción desconocida: " + accion); // La acción recibida no es ninguna de las conocidas
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // Responde 500 si ocurre cualquier error
            out.print("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        } finally {
            if (em != null && em.isOpen()) em.close(); // Siempre cierra el EntityManager para evitar leaks de conexión
        }
    }

    // Método auxiliar: responde 400 Bad Request con un mensaje de error en JSON
    private void bad(HttpServletResponse response, PrintWriter out, String msg) throws IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // HTTP 400
        out.print("{\"error\":\"" + escapeJson(msg) + "\"}"); // JSON con el mensaje de error
    }

    // Escapa caracteres especiales para que no rompan el JSON de salida
    private String escapeJson(String s) {
        if (s == null) return ""; // Si el string es null retorna vacío
        return s.replace("\\", "\\\\") // Escapa backslashes
                .replace("\"", "\\\"") // Escapa comillas dobles
                .replace("\n", "\\n").replace("\r", "\\r"); // Escapa saltos de línea
    }
}
