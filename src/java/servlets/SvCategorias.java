package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import logica.Categoria;
import persistencias.CategoriaJpaController;
import persistencias.JpaProvider;

/**
 * SvCategorias — Servlet de gestión de categorías de productos.
 * GET: retorna la lista de todas las categorías en formato JSON con el conteo de productos vinculados.
 * POST accion=eliminar: eliminación física. Bloqueada si la categoría tiene productos.
 * POST accion=editar: actualiza nombre y descripción de una categoría existente.
 * POST accion=desactivar: activa o desactiva una categoría (toggle).
 * POST sin accion: crea una categoría nueva.
 * Todas las operaciones POST requieren el permiso GESTIONAR_CATEGORIAS.
 */
public class SvCategorias extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            List<Categoria> lista = new CategoriaJpaController().findCategoriaEntities(); // traer todas desde BD
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < lista.size(); i++) {
                Categoria c = lista.get(i);
                if (i > 0) sb.append(",");
                sb.append("{");
                sb.append("\"id\":").append(c.getIdCategoria()).append(",");
                sb.append("\"nombre\":\"").append(escapeJson(c.getNombreCategoria())).append("\",");
                sb.append("\"descripcion\":\"").append(escapeJson(c.getDescripcion())).append("\",");
                sb.append("\"activo\":").append(c.isActivo()).append(",");
                // Contar cuántos productos están vinculados a esta categoría (necesario para bloquear eliminación)
                sb.append("\"productos\":").append(c.getProductos() != null ? c.getProductos().size() : 0);
                sb.append("}");
            }
            sb.append("]");
            out.print(sb.toString());

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        EntityManager em = null;
        try {
            if (!AuthHelper.tienePermiso(request, "GESTIONAR_CATEGORIAS")) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print("{\"error\":\"Sin permiso: GESTIONAR_CATEGORIAS\"}");
                return;
            }

            String accion = request.getParameter("accion");
            CategoriaJpaController ctrl = new CategoriaJpaController();

            if ("eliminar".equals(accion)) {
                // ELIMINAR: verificar primero que no haya productos vinculados a esta categoría
                int id = Integer.parseInt(request.getParameter("id"));
                em = JpaProvider.getEntityManagerFactory().createEntityManager();
                TypedQuery<Long> q = em.createQuery(
                    "SELECT COUNT(p) FROM Producto p WHERE p.categoria.idCategoria = :id", Long.class);
                q.setParameter("id", id);
                long count = q.getSingleResult();
                // Bloquear si hay productos: eliminar la categoría rompería la integridad referencial
                if (count > 0) {
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                    out.print("{\"error\":\"No se puede eliminar: tiene " + count + " producto(s) vinculado(s)\"}");
                    return;
                }
                ctrl.destroy(id); // eliminación física en BD
                out.print("{\"ok\":true}");
                return;
            }

            String nombre      = request.getParameter("nombre");      // nombre de la categoría
            String descripcion = request.getParameter("descripcion"); // descripción opcional
            String idStr       = request.getParameter("id");          // ID al editar o desactivar

            if (nombre == null || nombre.isBlank()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"El nombre es obligatorio\"}");
                return;
            }
            if (descripcion != null && descripcion.length() > 120) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"La descripci\\u00f3n no puede superar los 120 caracteres\"}");
                return;
            }

            // Verificar que no exista otra categoría con el mismo nombre (insensible a mayúsculas)
            // Al editar, excluir la propia categoría del chequeo de duplicados
            em = JpaProvider.getEntityManagerFactory().createEntityManager();
            TypedQuery<Long> dupQ = em.createQuery(
                "SELECT COUNT(c) FROM Categoria c WHERE LOWER(c.nombreCategoria) = LOWER(:n)" +
                (idStr != null ? " AND c.idCategoria <> :id" : ""), Long.class);
            dupQ.setParameter("n", nombre.trim());
            if (idStr != null) dupQ.setParameter("id", Integer.parseInt(idStr));
            if (dupQ.getSingleResult() > 0) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                out.print("{\"error\":\"Ya existe una categor&#237;a con ese nombre\"}");
                return;
            }

            if ("editar".equals(accion) && idStr != null) {
                // EDITAR: actualizar nombre y descripción
                Categoria cat = ctrl.findCategoria(Integer.parseInt(idStr));
                cat.setNombreCategoria(nombre);
                cat.setDescripcion(descripcion);
                ctrl.edit(cat); // UPDATE en BD
            } else if ("desactivar".equals(accion) && idStr != null) {
                // DESACTIVAR/ACTIVAR: alternancia del estado activo (toggle)
                Categoria cat = ctrl.findCategoria(Integer.parseInt(idStr));
                cat.setActivo(!cat.isActivo()); // si estaba activa pasa a inactiva y viceversa
                ctrl.edit(cat); // UPDATE en BD
            } else {
                // CREAR: nueva categoría activa por defecto
                Categoria cat = new Categoria();
                cat.setNombreCategoria(nombre);
                cat.setDescripcion(descripcion);
                cat.setActivo(true);
                ctrl.create(cat); // INSERT en BD
            }
            out.print("{\"ok\":true}");

        } catch (Exception e) {
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
