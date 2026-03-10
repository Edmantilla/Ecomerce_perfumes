package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import logica.Permiso;
import logica.Rol;
import logica.Rolpermiso;
import persistencias.JpaProvider;
import persistencias.PermisoJpaController;
import persistencias.RolJpaController;
import persistencias.RolpermisoJpaController;

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
@WebServlet(name = "SvPermisos", urlPatterns = {"/SvPermisos"})
public class SvPermisos extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        if (!AuthHelper.tienePermiso(request, "GESTIONAR_ROLES")) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            out.print("{\"error\":\"Sin permiso: GESTIONAR_ROLES\"}");
            return;
        }

        // Determinar qué recurso se pide: "permisos" lista todos los permisos; por defecto lista roles
        String recurso = request.getParameter("recurso");
        EntityManager em = null;
        try {
            em = JpaProvider.getEntityManagerFactory().createEntityManager();

            if ("permisos".equals(recurso)) {
                // Retornar la lista plana de todos los permisos del sistema
                List<Permiso> permisos = new PermisoJpaController().findPermisoEntities();
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < permisos.size(); i++) {
                    Permiso p = permisos.get(i);
                    if (i > 0) sb.append(",");
                    sb.append("{");
                    sb.append("\"id\":").append(p.getIdPermiso()).append(",");
                    sb.append("\"nombre\":\"").append(escapeJson(p.getNombrePermiso())).append("\",");
                    sb.append("\"descripcion\":\"").append(escapeJson(p.getDescripcion())).append("\",");
                    sb.append("\"modulo\":\"").append(escapeJson(p.getModulo())).append("\",");
                    sb.append("\"activo\":").append(p.isActivo());
                    sb.append("}");
                }
                sb.append("]");
                out.print(sb.toString());
                return;
            }

            // Por defecto: retornar roles con sus permisos anidados
            // El panel admin usa este endpoint para mostrar la tabla de roles y asignaciones
            List<Rol> roles = new RolJpaController().findRolEntities();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < roles.size(); i++) {
                Rol rol = roles.get(i);
                if (i > 0) sb.append(",");

                // Consultar los permisos asignados a este rol via la tabla rolpermiso
                TypedQuery<Rolpermiso> qrp = em.createQuery(
                    "SELECT rp FROM Rolpermiso rp WHERE rp.rol.idRol = :id", Rolpermiso.class);
                qrp.setParameter("id", rol.getIdRol());
                List<Rolpermiso> rps = qrp.getResultList();

                sb.append("{");
                sb.append("\"id\":").append(rol.getIdRol()).append(",");
                sb.append("\"nombre\":\"").append(escapeJson(rol.getNombreRol())).append("\",");
                sb.append("\"descripcion\":\"").append(escapeJson(rol.getDescripcion())).append("\",");
                sb.append("\"activo\":").append(rol.isActivo()).append(",");
                // Anidar los permisos dentro del objeto del rol
                sb.append("\"permisos\":[");
                for (int j = 0; j < rps.size(); j++) {
                    Rolpermiso rp = rps.get(j);
                    Permiso p = rp.getPermiso();
                    if (p == null) continue; // ignorar asignaciones con permiso nulo
                    if (j > 0) sb.append(",");
                    sb.append("{");
                    sb.append("\"idRolPermiso\":").append(rp.getIdRolPermiso()).append(","); // ID de la asignación (para revocar)
                    sb.append("\"idPermiso\":").append(p.getIdPermiso()).append(",");
                    sb.append("\"nombre\":\"").append(escapeJson(p.getNombrePermiso())).append("\",");
                    sb.append("\"modulo\":\"").append(escapeJson(p.getModulo())).append("\",");
                    sb.append("\"activo\":").append(p.isActivo());
                    sb.append("}");
                }
                sb.append("]}");
            }
            sb.append("]");
            out.print(sb.toString());

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        } finally {
            if (em != null && em.isOpen()) em.close();
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

        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        if (!AuthHelper.tienePermiso(request, "GESTIONAR_ROLES")) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            out.print("{\"error\":\"Sin permiso: GESTIONAR_ROLES\"}");
            return;
        }

        EntityManager em = null;
        try {
            String accion = request.getParameter("accion");
            em = JpaProvider.getEntityManagerFactory().createEntityManager();

            switch (accion != null ? accion : "") {

                case "crearPermiso": {
                    // Crear un nuevo permiso del sistema (ej: "VER_REPORTES", "EXPORTAR_DATOS")
                    String nombre = request.getParameter("nombre");
                    if (nombre == null || nombre.isBlank()) {
                        bad(response, out, "El nombre es obligatorio"); return;
                    }
                    // Verificar que no exista otro permiso con el mismo nombre
                    TypedQuery<Long> dup = em.createQuery(
                        "SELECT COUNT(p) FROM Permiso p WHERE LOWER(p.nombrePermiso) = LOWER(:n)", Long.class);
                    dup.setParameter("n", nombre.trim());
                    if (dup.getSingleResult() > 0) {
                        bad(response, out, "Ya existe un permiso con ese nombre"); return;
                    }
                    Permiso p = new Permiso();
                    p.setNombrePermiso(nombre.trim());
                    p.setDescripcion(request.getParameter("descripcion"));
                    p.setModulo(request.getParameter("modulo")); // módulo al que pertenece (ej: "PRODUCTOS")
                    p.setActivo(true);
                    new PermisoJpaController().create(p); // INSERT en BD
                    out.print("{\"ok\":true}");
                    break;
                }

                case "editarPermiso": {
                    // Actualizar los datos de un permiso existente
                    int id = Integer.parseInt(request.getParameter("id"));
                    String nombre = request.getParameter("nombre");
                    if (nombre == null || nombre.isBlank()) {
                        bad(response, out, "El nombre es obligatorio"); return;
                    }
                    PermisoJpaController ctrl = new PermisoJpaController();
                    Permiso p = ctrl.findPermiso(id);
                    if (p == null) { bad(response, out, "Permiso no encontrado"); return; }
                    p.setNombrePermiso(nombre.trim());
                    p.setDescripcion(request.getParameter("descripcion"));
                    p.setModulo(request.getParameter("modulo"));
                    ctrl.edit(p); // UPDATE en BD
                    out.print("{\"ok\":true}");
                    break;
                }

                case "togglePermiso": {
                    // Activar o desactivar un permiso (toggle). Los permisos inactivos no
                    // son evaluados por AuthHelper.tienePermiso() aunque estén asignados al rol
                    int id = Integer.parseInt(request.getParameter("id"));
                    PermisoJpaController ctrl = new PermisoJpaController();
                    Permiso p = ctrl.findPermiso(id);
                    if (p == null) { bad(response, out, "Permiso no encontrado"); return; }
                    p.setActivo(!p.isActivo()); // toggle activo/inactivo
                    ctrl.edit(p); // UPDATE en BD
                    out.print("{\"ok\":true}");
                    break;
                }

                case "crearRol": {
                    // Crear un nuevo rol (ej: "SUPERVISOR", "VENDEDOR")
                    String nombre = request.getParameter("nombre");
                    if (nombre == null || nombre.isBlank()) {
                        bad(response, out, "El nombre es obligatorio"); return;
                    }
                    // Verificar que no exista otro rol con el mismo nombre
                    TypedQuery<Long> dup = em.createQuery(
                        "SELECT COUNT(r) FROM Rol r WHERE LOWER(r.nombreRol) = LOWER(:n)", Long.class);
                    dup.setParameter("n", nombre.trim());
                    if (dup.getSingleResult() > 0) {
                        bad(response, out, "Ya existe un rol con ese nombre"); return;
                    }
                    Rol rol = new Rol();
                    rol.setNombreRol(nombre.trim());
                    rol.setDescripcion(request.getParameter("descripcion"));
                    rol.setActivo(true);
                    new RolJpaController().create(rol); // INSERT en BD
                    out.print("{\"ok\":true}");
                    break;
                }

                case "editarRol": {
                    // Actualizar el nombre y descripción de un rol existente
                    int id = Integer.parseInt(request.getParameter("id"));
                    String nombre = request.getParameter("nombre");
                    if (nombre == null || nombre.isBlank()) {
                        bad(response, out, "El nombre es obligatorio"); return;
                    }
                    RolJpaController ctrl = new RolJpaController();
                    Rol rol = ctrl.findRol(id);
                    if (rol == null) { bad(response, out, "Rol no encontrado"); return; }
                    rol.setNombreRol(nombre.trim());
                    rol.setDescripcion(request.getParameter("descripcion"));
                    ctrl.edit(rol); // UPDATE en BD
                    out.print("{\"ok\":true}");
                    break;
                }

                case "toggleRol": {
                    // Activar o desactivar un rol. Bloqueado si el rol tiene usuarios activos asignados
                    // (desactivar el rol sin reasignar usuarios los dejaría sin acceso)
                    int id = Integer.parseInt(request.getParameter("id"));
                    RolJpaController ctrl = new RolJpaController();
                    Rol rol = ctrl.findRol(id);
                    if (rol == null) { bad(response, out, "Rol no encontrado"); return; }
                    if (rol.isActivo()) {
                        // Solo bloquear si se intenta DESACTIVAR un rol con usuarios activos
                        TypedQuery<Long> q = em.createQuery(
                            "SELECT COUNT(u) FROM Usuario u WHERE u.rol.idRol = :id AND u.activo = true", Long.class);
                        q.setParameter("id", id);
                        if (q.getSingleResult() > 0) {
                            bad(response, out, "No se puede desactivar: tiene usuarios activos asignados"); return;
                        }
                    }
                    rol.setActivo(!rol.isActivo()); // toggle activo/inactivo
                    ctrl.edit(rol); // UPDATE en BD
                    out.print("{\"ok\":true}");
                    break;
                }

                case "asignar": {
                    // Asignar un permiso a un rol (crea una fila en la tabla rolpermiso)
                    int idRol     = Integer.parseInt(request.getParameter("idRol"));
                    int idPermiso = Integer.parseInt(request.getParameter("idPermiso"));
                    // Verificar que no esté ya asignado para evitar duplicados
                    TypedQuery<Long> dup = em.createQuery(
                        "SELECT COUNT(rp) FROM Rolpermiso rp WHERE rp.rol.idRol = :r AND rp.permiso.idPermiso = :p",
                        Long.class);
                    dup.setParameter("r", idRol).setParameter("p", idPermiso);
                    if (dup.getSingleResult() > 0) {
                        bad(response, out, "El permiso ya está asignado a este rol"); return;
                    }
                    RolJpaController rolCtrl = new RolJpaController();
                    PermisoJpaController perCtrl = new PermisoJpaController();
                    Rol rol = rolCtrl.findRol(idRol);
                    Permiso permiso = perCtrl.findPermiso(idPermiso);
                    if (rol == null || permiso == null) {
                        bad(response, out, "Rol o permiso no encontrado"); return;
                    }
                    Rolpermiso rp = new Rolpermiso(rol, permiso); // crear la asignación
                    new RolpermisoJpaController().create(rp); // INSERT en tabla rolpermiso
                    out.print("{\"ok\":true}");
                    break;
                }

                case "revocar": {
                    // Revocar un permiso de un rol: elimina la fila en rolpermiso por su ID
                    int idRolPermiso = Integer.parseInt(request.getParameter("idRolPermiso"));
                    new RolpermisoJpaController().destroy(idRolPermiso); // DELETE en BD
                    out.print("{\"ok\":true}");
                    break;
                }

                default:
                    bad(response, out, "Acción desconocida: " + accion);
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        } finally {
            if (em != null && em.isOpen()) em.close();
        }
    }

    private void bad(HttpServletResponse response, PrintWriter out, String msg) throws IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        out.print("{\"error\":\"" + escapeJson(msg) + "\"}");
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }
}
