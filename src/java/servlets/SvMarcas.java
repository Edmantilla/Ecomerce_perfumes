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
import logica.Marca;
import persistencias.JpaProvider;
import persistencias.MarcaJpaController;

public class SvMarcas extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            List<Marca> lista = new MarcaJpaController().findMarcaEntities();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < lista.size(); i++) {
                Marca m = lista.get(i);
                if (i > 0) sb.append(",");
                sb.append("{");
                sb.append("\"id\":").append(m.getIdMarca()).append(",");
                sb.append("\"nombre\":\"").append(escapeJson(m.getNombreMarca())).append("\",");
                sb.append("\"descripcion\":\"").append(escapeJson(m.getDescripcion())).append("\",");
                sb.append("\"activo\":").append(m.isActivo()).append(",");
                sb.append("\"productos\":").append(m.getProductos() != null ? m.getProductos().size() : 0);
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
            String accion = request.getParameter("accion");
            MarcaJpaController ctrl = new MarcaJpaController();

            if ("eliminar".equals(accion)) {
                int id = Integer.parseInt(request.getParameter("id"));
                em = JpaProvider.getEntityManagerFactory().createEntityManager();
                TypedQuery<Long> q = em.createQuery(
                    "SELECT COUNT(p) FROM Producto p WHERE p.marca.idMarca = :id", Long.class);
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
                "SELECT COUNT(m) FROM Marca m WHERE LOWER(m.nombreMarca) = LOWER(:n)" +
                (idStr != null ? " AND m.idMarca <> :id" : ""), Long.class);
            dupQ.setParameter("n", nombre.trim());
            if (idStr != null) dupQ.setParameter("id", Integer.parseInt(idStr));
            if (dupQ.getSingleResult() > 0) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                out.print("{\"error\":\"Ya existe una marca con ese nombre\"}");
                return;
            }

            if ("editar".equals(accion) && idStr != null) {
                Marca marca = ctrl.findMarca(Integer.parseInt(idStr));
                marca.setNombreMarca(nombre);
                marca.setDescripcion(descripcion);
                ctrl.edit(marca);
            } else if ("desactivar".equals(accion) && idStr != null) {
                Marca marca = ctrl.findMarca(Integer.parseInt(idStr));
                em.close(); em = null;
                em = JpaProvider.getEntityManagerFactory().createEntityManager();
                TypedQuery<Long> qp = em.createQuery(
                    "SELECT COUNT(p) FROM Producto p WHERE p.marca.idMarca = :id AND p.activo = true", Long.class);
                qp.setParameter("id", Integer.parseInt(idStr));
                if (marca.isActivo() && qp.getSingleResult() > 0) {
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                    out.print("{\"error\":\"No se puede desactivar: tiene productos activos vinculados\"}");
                    return;
                }
                marca.setActivo(!marca.isActivo());
                ctrl.edit(marca);
            } else {
                Marca marca = new Marca();
                marca.setNombreMarca(nombre);
                marca.setDescripcion(descripcion);
                marca.setActivo(true);
                ctrl.create(marca);
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
