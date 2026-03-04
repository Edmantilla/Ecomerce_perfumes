package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import logica.Permiso;
import logica.Rol;
import logica.Rolpermiso;
import persistencias.JpaProvider;
import persistencias.PermisoJpaController;
import persistencias.RolJpaController;
import persistencias.RolpermisoJpaController;

public class SvPermisos extends HttpServlet {

    /**
     * GET /SvPermisos                  -> todos los roles con sus permisos
     * GET /SvPermisos?recurso=permisos -> lista completa de permisos
     * GET /SvPermisos?recurso=roles    -> lista de roles
     */
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

        String recurso = request.getParameter("recurso");
        EntityManager em = null;
        try {
            em = JpaProvider.getEntityManagerFactory().createEntityManager();

            if ("permisos".equals(recurso)) {
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

            // Default: roles con sus permisos asignados
            List<Rol> roles = new RolJpaController().findRolEntities();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < roles.size(); i++) {
                Rol rol = roles.get(i);
                if (i > 0) sb.append(",");

                // Permisos asignados a este rol
                TypedQuery<Rolpermiso> qrp = em.createQuery(
                    "SELECT rp FROM Rolpermiso rp WHERE rp.rol.idRol = :id", Rolpermiso.class);
                qrp.setParameter("id", rol.getIdRol());
                List<Rolpermiso> rps = qrp.getResultList();

                sb.append("{");
                sb.append("\"id\":").append(rol.getIdRol()).append(",");
                sb.append("\"nombre\":\"").append(escapeJson(rol.getNombreRol())).append("\",");
                sb.append("\"descripcion\":\"").append(escapeJson(rol.getDescripcion())).append("\",");
                sb.append("\"activo\":").append(rol.isActivo()).append(",");
                sb.append("\"permisos\":[");
                for (int j = 0; j < rps.size(); j++) {
                    Rolpermiso rp = rps.get(j);
                    Permiso p = rp.getPermiso();
                    if (p == null) continue;
                    if (j > 0) sb.append(",");
                    sb.append("{");
                    sb.append("\"idRolPermiso\":").append(rp.getIdRolPermiso()).append(",");
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
                    String nombre = request.getParameter("nombre");
                    if (nombre == null || nombre.isBlank()) {
                        bad(response, out, "El nombre es obligatorio"); return;
                    }
                    // Duplicado check
                    TypedQuery<Long> dup = em.createQuery(
                        "SELECT COUNT(p) FROM Permiso p WHERE LOWER(p.nombrePermiso) = LOWER(:n)", Long.class);
                    dup.setParameter("n", nombre.trim());
                    if (dup.getSingleResult() > 0) {
                        bad(response, out, "Ya existe un permiso con ese nombre"); return;
                    }
                    Permiso p = new Permiso();
                    p.setNombrePermiso(nombre.trim());
                    p.setDescripcion(request.getParameter("descripcion"));
                    p.setModulo(request.getParameter("modulo"));
                    p.setActivo(true);
                    new PermisoJpaController().create(p);
                    out.print("{\"ok\":true}");
                    break;
                }

                case "editarPermiso": {
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
                    ctrl.edit(p);
                    out.print("{\"ok\":true}");
                    break;
                }

                case "togglePermiso": {
                    int id = Integer.parseInt(request.getParameter("id"));
                    PermisoJpaController ctrl = new PermisoJpaController();
                    Permiso p = ctrl.findPermiso(id);
                    if (p == null) { bad(response, out, "Permiso no encontrado"); return; }
                    p.setActivo(!p.isActivo());
                    ctrl.edit(p);
                    out.print("{\"ok\":true}");
                    break;
                }

                case "crearRol": {
                    String nombre = request.getParameter("nombre");
                    if (nombre == null || nombre.isBlank()) {
                        bad(response, out, "El nombre es obligatorio"); return;
                    }
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
                    new RolJpaController().create(rol);
                    out.print("{\"ok\":true}");
                    break;
                }

                case "editarRol": {
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
                    ctrl.edit(rol);
                    out.print("{\"ok\":true}");
                    break;
                }

                case "toggleRol": {
                    int id = Integer.parseInt(request.getParameter("id"));
                    RolJpaController ctrl = new RolJpaController();
                    Rol rol = ctrl.findRol(id);
                    if (rol == null) { bad(response, out, "Rol no encontrado"); return; }
                    // Verificar que no sea el único rol de admin activo
                    if (rol.isActivo()) {
                        TypedQuery<Long> q = em.createQuery(
                            "SELECT COUNT(u) FROM Usuario u WHERE u.rol.idRol = :id AND u.activo = true", Long.class);
                        q.setParameter("id", id);
                        if (q.getSingleResult() > 0) {
                            bad(response, out, "No se puede desactivar: tiene usuarios activos asignados"); return;
                        }
                    }
                    rol.setActivo(!rol.isActivo());
                    ctrl.edit(rol);
                    out.print("{\"ok\":true}");
                    break;
                }

                case "asignar": {
                    int idRol     = Integer.parseInt(request.getParameter("idRol"));
                    int idPermiso = Integer.parseInt(request.getParameter("idPermiso"));
                    // Evitar duplicado
                    TypedQuery<Long> dup = em.createQuery(
                        "SELECT COUNT(rp) FROM Rolpermiso rp WHERE rp.rol.idRol = :r AND rp.permiso.idPermiso = :p",
                        Long.class);
                    dup.setParameter("r", idRol).setParameter("p", idPermiso);
                    if (dup.getSingleResult() > 0) {
                        bad(response, out, "El permiso ya est\u00e1 asignado a este rol"); return;
                    }
                    RolJpaController rolCtrl = new RolJpaController();
                    PermisoJpaController perCtrl = new PermisoJpaController();
                    Rol rol = rolCtrl.findRol(idRol);
                    Permiso permiso = perCtrl.findPermiso(idPermiso);
                    if (rol == null || permiso == null) {
                        bad(response, out, "Rol o permiso no encontrado"); return;
                    }
                    Rolpermiso rp = new Rolpermiso(rol, permiso);
                    new RolpermisoJpaController().create(rp);
                    out.print("{\"ok\":true}");
                    break;
                }

                case "revocar": {
                    int idRolPermiso = Integer.parseInt(request.getParameter("idRolPermiso"));
                    new RolpermisoJpaController().destroy(idRolPermiso);
                    out.print("{\"ok\":true}");
                    break;
                }

                default:
                    bad(response, out, "Acci\u00f3n desconocida: " + accion);
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
