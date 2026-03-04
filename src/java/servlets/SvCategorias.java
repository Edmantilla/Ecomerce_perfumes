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

public class SvCategorias extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            List<Categoria> lista = new CategoriaJpaController().findCategoriaEntities();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < lista.size(); i++) {
                Categoria c = lista.get(i);
                if (i > 0) sb.append(",");
                sb.append("{");
                sb.append("\"id\":").append(c.getIdCategoria()).append(",");
                sb.append("\"nombre\":\"").append(escapeJson(c.getNombreCategoria())).append("\",");
                sb.append("\"descripcion\":\"").append(escapeJson(c.getDescripcion())).append("\",");
                sb.append("\"activo\":").append(c.isActivo()).append(",");
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
                int id = Integer.parseInt(request.getParameter("id"));
                em = JpaProvider.getEntityManagerFactory().createEntityManager();
                TypedQuery<Long> q = em.createQuery(
                    "SELECT COUNT(p) FROM Producto p WHERE p.categoria.idCategoria = :id", Long.class);
                q.setParameter("id", id);
                long count = q.getSingleResult();
                if (count > 0) {
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                    out.print("{\"error\":\"No se puede eliminar: tiene " + count + " producto(s) vinculado(s)\"}");
                    return;
                }
                ctrl.destroy(id);
                out.print("{\"ok\":true}");
                return;
            }

            String nombre      = request.getParameter("nombre");
            String descripcion = request.getParameter("descripcion");
            String idStr       = request.getParameter("id");

            if (nombre == null || nombre.isBlank()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"El nombre es obligatorio\"}");
                return;
            }

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
                Categoria cat = ctrl.findCategoria(Integer.parseInt(idStr));
                cat.setNombreCategoria(nombre);
                cat.setDescripcion(descripcion);
                ctrl.edit(cat);
            } else if ("desactivar".equals(accion) && idStr != null) {
                Categoria cat = ctrl.findCategoria(Integer.parseInt(idStr));
                cat.setActivo(!cat.isActivo());
                ctrl.edit(cat);
            } else {
                Categoria cat = new Categoria();
                cat.setNombreCategoria(nombre);
                cat.setDescripcion(descripcion);
                cat.setActivo(true);
                ctrl.create(cat);
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
